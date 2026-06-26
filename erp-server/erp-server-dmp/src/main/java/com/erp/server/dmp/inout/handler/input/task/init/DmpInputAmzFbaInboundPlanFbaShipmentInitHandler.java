package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FBA InboundPlan 详情拉取 - 拉取FBA货件前置处理器。
 * <p>
 * 手动 hotfix（根任务 extendJson 含 {@code shipmentCodeList}）对拉取失败执行 fail-fast，与
 * {@link DmpInputAmzCommonInitHandler#hasManualShipmentCodeFilter()} 策略一致。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanFbaShipmentInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> parentMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(parentMongoData)) {
            if (hasManualShipmentCodeFilter()) {
                throw new ServiceException("手动拉取FBA入库计划详情失败: 上游入库计划列表为空, taskId=" + dmpInputTaskEntity.getId());
            }
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

        boolean manualPull = hasManualShipmentCodeFilter();
        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN_DETAIL;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (isRateLimited(limitKey)) {
            if (manualPull) {
                throw new ServiceException("手动拉取FBA入库计划详情失败: Amazon API 429 限流等待恢复, taskId=" + dmpInputTaskEntity.getId());
            }
            log.warn("【FBA入库计划详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        List<JSONObject> inboundPlanDataList = new ArrayList<>();
        for (Map<String, Object> parentMongo : parentMongoData) {
            String inboundPlanId = firstNonBlankString(parentMongo, "inboundPlanId");
            if (StringUtils.isBlank(inboundPlanId)) {
                continue;
            }
            try {
                InboundPlan inboundPlan = api.getInboundPlan(inboundPlanId);
                if (inboundPlan == null) {
                    if (manualPull) {
                        throw new ServiceException("手动拉取FBA入库计划详情失败: getInboundPlan返回空, inboundPlanId=" + inboundPlanId
                                + ", taskId=" + dmpInputTaskEntity.getId());
                    }
                    continue;
                }
                inboundPlanDataList.add((JSONObject) JSON.toJSON(inboundPlan));
            } catch (ApiException e) {
                if (e.getCode() == 429 && manualPull) {
                    applyRateLimitBackoff(requestType, limitKey);
                    disableNextStatus(dmpResponse);
                    throw new ServiceException("手动拉取FBA入库计划详情失败: Amazon API 429 限流, taskId=" + dmpInputTaskEntity.getId());
                }
                if (handleRateLimitAndCheckNeedStop(e, requestType, limitKey, shopInfoDTO, dmpResponse, "FBA入库计划详情拉取")) {
                    return Collections.emptyList();
                }
                if (manualPull) {
                    throw new ServiceException("手动拉取FBA入库计划详情失败, inboundPlanId=" + inboundPlanId + ", error=" + e.getMessage());
                }
                log.warn("跳过inboundPlanId={}, 原因={}", inboundPlanId, e.getMessage());
            } catch (LWAException e) {
                if (manualPull) {
                    throw new ServiceException("手动拉取FBA入库计划详情失败, inboundPlanId=" + inboundPlanId + ", error=" + e.getMessage());
                }
                log.warn("跳过inboundPlanId={}, 原因={}", inboundPlanId, e.getMessage());
            }
        }

        if (CollUtil.isEmpty(inboundPlanDataList)) {
            if (manualPull) {
                throw new ServiceException("手动拉取FBA入库计划详情失败: 未拉取到任何计划详情, taskId=" + dmpInputTaskEntity.getId());
            }
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(inboundPlanDataList)));
    }
}
