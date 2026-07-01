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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        // 业务确认 Shopee Webhook 当前无需签名校验，保留空实现以兼容统一 WebhookHandler 流程。
    }

    @Override
    public WebhookResult process(String data, Map<String, String> headers, String serviceFlag) {
        // WebhookController 对非奇门回调统一返回 HTTP 200；这里的 flag/code 作为业务处理结果写入响应体和日志。
        if(StringUtils.isBlank(data)){
            return WebhookResult.isSuccess("fail", 400, "回传数据为空");
        }
        log.warn("虾皮webhook 获取数据,payloadLength={}", safeLength(data));
        try {
            JSONObject json = JSONUtil.parseObj(data);
            Integer code = json.getInt("code");
            if (!Objects.equals(ORDER_STATUS_CODE, code)) {
                log.info("虾皮webhook 忽略非订单状态事件，code={}", code);
                return WebhookResult.isSuccess();
            }
            JSONObject webhookData = json.getJSONObject("data");
            if (Objects.isNull(webhookData)) {
                log.warn("虾皮webhook data为空，payloadLength={}", safeLength(data));
                return WebhookResult.isSuccess("fail", 400, "data为空");
            }
            String ordersn = webhookData.getStr("ordersn");
            String status = webhookData.getStr("status");
            Long updateTime = webhookData.getLong("update_time");
            String platformShopId = json.getStr("shop_id");
            log.warn("虾皮webhook 订单状态事件，code={}，ordersn={}，status={}，updateTime={}，shopId={}",
                    code, ordersn, status, updateTime, platformShopId);
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
            // Shopee webhook may replay; duplicate order effects are handled by the downstream DMP import business keys.
            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        } catch (Exception e) {
            log.error("【Shopee Webhook】处理失败，payloadLength={}，错误={}", safeLength(data), e.getMessage(), e);
            return WebhookResult.isSuccess("fail", 500, "处理失败");
        } finally {
            ThirdWarehouseContext.remove();
        }
        return WebhookResult.isSuccess();
    }

    private int safeLength(String data) {
        return data == null ? 0 : data.length();
    }

    private DmpCfgInputDetailEntity resolveDetailEntity(String cfgInputId, String platformShopId) {
        String erpShopId = resolveErpShopId(platformShopId);
        List<String> nextLevelIds = buildDetailNextLevelIds(erpShopId, platformShopId);
        List<DmpCfgInputDetailEntity> activeDetails = dmpCfgInputDetailService.lambdaQuery()
                .eq(DmpCfgInputDetailEntity::getMainId, cfgInputId)
                .eq(DmpCfgInputDetailEntity::getDisabled, Boolean.FALSE)
                .and(wrapper -> wrapper.in(DmpCfgInputDetailEntity::getNextLevelId, nextLevelIds)
                        .or()
                        .isNull(DmpCfgInputDetailEntity::getNextLevelId))
                .list();
        if (activeDetails == null || activeDetails.isEmpty()) {
            log.warn("虾皮webhook 未查询到启用的店铺任务明细，cfgInputId={}，platformShopId={}，erpShopId={}",
                    cfgInputId, platformShopId, erpShopId);
            return null;
        }
        DmpCfgInputDetailEntity matchedDetail = findDetailByNextLevelId(activeDetails, erpShopId);
        if (Objects.isNull(matchedDetail) && !Objects.equals(erpShopId, platformShopId)) {
            matchedDetail = findDetailByNextLevelId(activeDetails, platformShopId);
        }
        if (Objects.nonNull(matchedDetail)) {
            return matchedDetail;
        }
        if (activeDetails.size() == 1) {
            DmpCfgInputDetailEntity fallbackDetail = activeDetails.get(0);
            if (Objects.nonNull(fallbackDetail)) {
                String nextLevelId = fallbackDetail.getNextLevelId();
                if (StringUtils.isBlank(nextLevelId)) {
                    return fallbackDetail;
                }
            }
        }
        log.warn("虾皮webhook 未匹配到店铺任务明细，cfgInputId={}，platformShopId={}，erpShopId={}",
                cfgInputId, platformShopId, erpShopId);
        return null;
    }

    private DmpCfgInputDetailEntity findDetailByNextLevelId(List<DmpCfgInputDetailEntity> activeDetails, String nextLevelId) {
        if (StringUtils.isBlank(nextLevelId)) {
            return null;
        }
        return activeDetails.stream()
                .filter(detail -> nextLevelId.equals(detail.getNextLevelId()))
                .findFirst()
                .orElse(null);
    }

    private List<String> buildDetailNextLevelIds(String erpShopId, String platformShopId) {
        List<String> nextLevelIds = new java.util.ArrayList<>();
        if (!StringUtils.isBlank(erpShopId)) {
            nextLevelIds.add(erpShopId);
        }
        if (!StringUtils.isBlank(platformShopId) && !Objects.equals(erpShopId, platformShopId)) {
            nextLevelIds.add(platformShopId);
        }
        // 兼容历史单店铺配置：nextLevelId 为空时作为兜底任务明细。
        nextLevelIds.add("");
        return nextLevelIds.stream().distinct().collect(Collectors.toList());
    }

    private String resolveErpShopId(String platformShopId) {
        ThirdMappingEntity mapping = thirdMappingService.getShopByThirdCode(platformShopId, PlatformDictEnum.SHOPEE.getCode());
        if (Objects.isNull(mapping) || StringUtils.isBlank(mapping.getSysId())) {
            return "";
        }
        return mapping.getSysId();
    }

}
