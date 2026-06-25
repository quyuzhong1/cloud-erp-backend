package com.erp.server.dmp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.WebhookResult;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.ThirdMappingService;
import io.seata.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 订单出口回告处理类
 */
public class ShopeeWebhookHandler implements WebhookHandler{

    private static final Logger log = LoggerFactory.getLogger(ShopeeWebhookHandler.class);
    private static final String CFG_INPUT_CODE = "shopeeOrderWebhook";
    private static final Integer ORDER_STATUS_CODE = 3;
    private static final String SUCCESS = "success";

    private final DmpInputCreateFactory dmpInputCreateFactory = SpringUtil.getBean(DmpInputCreateFactory.class);
    private final DmpCfgInputService dmpCfgInputService = SpringUtil.getBean(DmpCfgInputService.class);
    private final ThirdMappingService thirdMappingService = SpringUtil.getBean(ThirdMappingService.class);
    private final DmpCfgInputDetailService dmpCfgInputDetailService = SpringUtil.getBean(DmpCfgInputDetailService.class);

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        // Shopee webhook signature verification is handled before dispatch.
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        if(StringUtils.isBlank(data)){
            return WebhookResult.isSuccess("fail", 400, "回传数据为空");
        }
        log.warn("虾皮webhook 获取数据,{}", data);
        try {
            JSONObject json = JSONUtil.parseObj(data);
            Integer code = json.getInt("code");
            if (!Objects.equals(ORDER_STATUS_CODE, code)) {
                log.info("虾皮webhook 忽略非订单状态事件，code={}", code);
                return WebhookResult.isSuccess();
            }
            JSONObject webhookData = json.getJSONObject("data");
            if (Objects.isNull(webhookData)) {
                log.warn("虾皮webhook data为空，payload={}", data);
                return WebhookResult.isSuccess("fail", 400, "data为空");
            }
            String ordersn = webhookData.getStr("ordersn");
            String status = webhookData.getStr("status");
            Long updateTime = webhookData.getLong("update_time");
            String platformShopId = json.getStr("shop_id");
            if (StringUtils.isBlank(ordersn) || StringUtils.isBlank(status) || Objects.isNull(updateTime) || StringUtils.isBlank(platformShopId)) {
                log.warn("虾皮webhook 关键字段为空，ordersn={}，status={}，updateTime={}，shopId={}",
                        ordersn, status, updateTime, platformShopId);
                return WebhookResult.isSuccess("fail", 400, "关键字段为空");
            }
            DmpCfgInputEntity cfgInputEntity = dmpCfgInputService.lambdaQuery()
                    .eq(DmpCfgInputEntity::getCode, CFG_INPUT_CODE)
                    .eq(DmpCfgInputEntity::getDisabled, Boolean.FALSE)
                    .last("limit 1")
                    .one();
            if (Objects.isNull(cfgInputEntity)) {
                log.warn("虾皮webhook 未找到任务配置，code={}", CFG_INPUT_CODE);
                return WebhookResult.isSuccess("fail", 500, "未找到任务配置");
            }
            DmpCfgInputDetailEntity detailEntity = resolveDetailEntity(cfgInputEntity.getId(), platformShopId);
            if (Objects.isNull(detailEntity)) {
                log.warn("虾皮webhook 未找到任务明细配置，cfgInputId={}，shopId={}",
                        cfgInputEntity.getId(), platformShopId);
                return WebhookResult.isSuccess("fail", 500, "未找到任务明细配置");
            }
            json.set("platformShopId", platformShopId);
            ThirdWarehouseContext.setData(json.toString());
            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
            dmpInputHotfixCreateRequest.setCfgInputId(cfgInputEntity.getId());
            dmpInputHotfixCreateRequest.setCfgInputDetailIdList(Collections.singletonList(detailEntity.getId()));
            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        } catch (Exception e) {
            log.error("【Shopee Webhook】处理失败，data={}，错误={}", data, e.getMessage(), e);
            return WebhookResult.isSuccess("fail", 500, "处理失败");
        } finally {
            ThirdWarehouseContext.remove();
        }
        return WebhookResult.isSuccess();
    }

    private DmpCfgInputDetailEntity resolveDetailEntity(String cfgInputId, String platformShopId) {
        String erpShopId = resolveErpShopId(platformShopId);
        DmpCfgInputDetailEntity matchedDetail = queryDetailByNextLevelId(cfgInputId, erpShopId);
        if (Objects.isNull(matchedDetail) && !Objects.equals(erpShopId, platformShopId)) {
            matchedDetail = queryDetailByNextLevelId(cfgInputId, platformShopId);
        }
        if (Objects.nonNull(matchedDetail)) {
            return matchedDetail;
        }
        int detailCount = dmpCfgInputDetailService.lambdaQuery()
                .eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
                .eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
                .count();
        if (detailCount == 1) {
            DmpCfgInputDetailEntity fallbackDetail = dmpCfgInputDetailService.lambdaQuery()
                    .eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
                    .eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
                    .and(query -> query.isNull(DmpCfgInputDetailEntity::getNextLevelId)
                            .or()
                            .eq(DmpCfgInputDetailEntity::getNextLevelId, ""))
                    .last("limit 1")
                    .one();
            if (Objects.nonNull(fallbackDetail)) {
                return fallbackDetail;
            }
        }
        log.warn("虾皮webhook 未匹配到店铺任务明细，cfgInputId={}，platformShopId={}，erpShopId={}",
                cfgInputId, platformShopId, erpShopId);
        return null;
    }

    private DmpCfgInputDetailEntity queryDetailByNextLevelId(String cfgInputId, String nextLevelId) {
        if (StringUtils.isBlank(nextLevelId)) {
            return null;
        }
        return dmpCfgInputDetailService.lambdaQuery()
                .eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
                .eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
                .eq(DmpCfgInputDetailEntity::getNextLevelId, nextLevelId)
                .last("limit 1")
                .one();
    }

    private String resolveErpShopId(String platformShopId) {
        ThirdMappingEntity mapping = thirdMappingService.getShopByThirdCode(platformShopId, PlatformDictEnum.SHOPEE.getCode());
        if (Objects.isNull(mapping) || StringUtils.isBlank(mapping.getSysId())) {
            return "";
        }
        return mapping.getSysId();
    }

}
