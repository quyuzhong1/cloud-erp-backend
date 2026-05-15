package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlan;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FBA InboundPlan 详情拉取 - 拉取FBA货件前置处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanFbaShipmentInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> parentMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(parentMongoData)) {
            log.warn("FBA入库计划详情主任务taskId={},结果为空无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }

        String shopId = resolveAuthShopIdByTaskChain();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("未找到店铺授权ID:taskId=" + dmpInputTaskEntity.getId());
        }
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN_DETAIL;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (redisUtil.get(limitKey) != null) {
            log.warn("【FBA入库计划详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        List<JSONObject> inboundPlanDataList = new ArrayList<>();
        for (Map<String, Object> parentMongo : parentMongoData) {
            String inboundPlanId = parentMongo.getOrDefault("inboundPlanId", "").toString();
            if (StringUtils.isBlank(inboundPlanId)) {
                continue;
            }
            try {
                InboundPlan inboundPlan = api.getInboundPlan(inboundPlanId);
                if (inboundPlan == null) {
                    continue;
                }
                inboundPlanDataList.add((JSONObject) JSON.toJSON(inboundPlan));
            } catch (ApiException e) {
                if (e.getCode() == 429) {
                    BigDecimal timeout = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(requestType.getRateLimit()), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, requestType.getRateLimit(), timeout.longValue());
                    log.warn("【FBA入库计划详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                    disableNextStatus(dmpResponse);
                    return Collections.emptyList();
                }
                log.warn("跳过inboundPlanId={}, 原因={}", inboundPlanId, e.getMessage());
            } catch (LWAException e) {
                log.warn("跳过inboundPlanId={}, 原因={}", inboundPlanId, e.getMessage());
            }
        }

        if (CollUtil.isEmpty(inboundPlanDataList)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(inboundPlanDataList)));
    }

    /**
     * 子任务 nextLevelId 会变成上游 mongoId，这里沿父任务链回溯拿根任务店铺ID。
     */
    private String resolveAuthShopIdByTaskChain() {
        DmpInputTaskEntity currentTask = dmpInputTaskEntity;
        int guard = 0;
        while (currentTask != null && StringUtils.isNotBlank(currentTask.getParentTaskId()) && guard++ < 20) {
            DmpInputTaskEntity parentTask = dmpInputTaskService.getById(currentTask.getParentTaskId());
            if (parentTask == null) {
                break;
            }
            currentTask = parentTask;
        }
        if (currentTask == null) {
            return "";
        }
        return StringUtils.defaultString(currentTask.getNextLevelId());
    }

    private String buildRateLimitKey(AmazonShopInfoDTO shopInfoDTO, AmazonRequestTypeRateLimiterEnum requestType) {
        return StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(),
                shopInfoDTO.getPlatformShopCode(), requestType.getBusinessTypeName());
    }

    private void disableNextStatus(DmpInputTaskResponse dmpResponse) {
        if (dmpResponse instanceof DmpInputInitResponse) {
            ((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
        }
    }
}
