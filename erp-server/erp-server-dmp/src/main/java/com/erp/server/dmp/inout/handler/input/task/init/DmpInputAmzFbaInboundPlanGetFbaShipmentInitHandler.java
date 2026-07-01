package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Shipment;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * FBA InboundPlan 货件详情拉取 - 拉取FBA货件新流程。
 * <p>
 * 审查问题3（intentional）：手动 hotfix（根任务 extendJson 含 {@code shipmentCodeList}）在列表/详情/429 等
 * 多数 Init 节点执行 fail-fast；本节点在「已 getShipment 成功但 shipmentConfirmationId 与输入货件号未精确匹配」
 * 或「429 且尚未命中指定号但同计划已有全量 getShipment 结果」时，回退落库同计划下已拉到的全部货件，
 * 避免字段映射差异/限流导致手动拉取完全空跑，可能同步计划内其它货件。此为产品确认的兜底策略，勿当缺陷修复。
 * 若 {@code allShipmentDetailDataMap} 为空（计划下无任何 getShipment 结果），手动场景仍 fail-fast。
 * 定时全量同步不走回退分支。变更需产品确认。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanGetFbaShipmentInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> parentMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(parentMongoData)) {
            if (hasManualShipmentCodeFilter()) {
                throw new ServiceException("手动拉取FBA货件详情失败: 上游入库计划详情为空, taskId=" + dmpInputTaskEntity.getId());
            }
            log.warn("FBA入库计划货件详情主任务taskId={},结果为空无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }

        DmpInputTaskEntity rootTask = resolveRootTaskByTaskChain();
        String shopId = rootTask == null ? "" : StringUtils.defaultString(rootTask.getNextLevelId());
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("未找到店铺授权ID:taskId=" + dmpInputTaskEntity.getId());
        }
        List<String> shipmentCodeList = parseShipmentCodeList(rootTask == null ? dmpInputTaskEntity.getExtendJson() : rootTask.getExtendJson());
        Set<String> shipmentCodeSet = new LinkedHashSet<>(shipmentCodeList);

        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        Map<String, JSONObject> shipmentDetailDataMap = new LinkedHashMap<>();
        Map<String, JSONObject> allShipmentDetailDataMap = new LinkedHashMap<>();
        Set<String> pendingShipmentCodeSet = new LinkedHashSet<>(shipmentCodeSet);
        boolean manualPull = hasManualShipmentCodeFilter();

        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN_SHIPMENT;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (isRateLimited(limitKey)) {
            if (manualPull) {
                throw new ServiceException("手动拉取FBA货件详情失败: Amazon API 429 限流等待恢复, taskId=" + dmpInputTaskEntity.getId());
            }
            log.warn("【FBA入库计划货件详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        for (Map<String, Object> parentMongo : parentMongoData) {
            String inboundPlanId = firstNonBlankString(parentMongo, "inboundPlanId");
            if (StringUtils.isBlank(inboundPlanId)) {
                continue;
            }
            Set<String> shipmentRawIdSet = parseShipmentRawIdSet(parentMongo);
            if (CollectionUtils.isEmpty(shipmentRawIdSet)) {
                continue;
            }
            for (String shipmentRawId : shipmentRawIdSet) {
                try {
                    Shipment shipmentDetail = api.getShipment(inboundPlanId, shipmentRawId);
                    if (shipmentDetail == null) {
                        log.warn("跳过shipment，inboundPlanId={}, shipmentId={}, 原因=getShipment返回空", inboundPlanId, shipmentRawId);
                        continue;
                    }
                    JSONObject shipmentDetailJson = (JSONObject) JSON.toJSON(shipmentDetail);
                    // getShipment 响应中通常不带 marketplaceId，需要从上游 getInboundPlan 结果传下来
                    shipmentDetailJson.putIfAbsent("inboundPlanId", inboundPlanId);
                    Object parentMarketplaceIdsObj = parentMongo.get("marketplaceIds");
                    if (parentMarketplaceIdsObj != null) {
                        shipmentDetailJson.putIfAbsent("marketplaceIds", parentMarketplaceIdsObj);
                    }
                    if (StringUtils.isBlank(shipmentDetailJson.getString("marketplaceId"))) {
                        String parentMarketplaceId = resolveParentMarketplaceId(parentMongo);
                        if (StringUtils.isNotBlank(parentMarketplaceId)) {
                            shipmentDetailJson.put("marketplaceId", parentMarketplaceId);
                        }
                    }
                    String shipmentCode = resolveShipmentCode(shipmentDetailJson, "");
                    String allStoreCode = StringUtils.isNotBlank(shipmentCode) ? shipmentCode : shipmentRawId;
                    allShipmentDetailDataMap.putIfAbsent(allStoreCode, shipmentDetailJson);
                    String storeCode;
                    if (CollUtil.isNotEmpty(shipmentCodeSet)) {
                        if (!shipmentCodeSet.contains(shipmentCode)) {
                            continue;
                        }
                        storeCode = shipmentCode;
                    } else {
                        storeCode = StringUtils.isNotBlank(shipmentCode) ? shipmentCode : shipmentRawId;
                    }
                    shipmentDetailDataMap.putIfAbsent(storeCode, shipmentDetailJson);
                    pendingShipmentCodeSet.remove(shipmentCode);
                    if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isEmpty(pendingShipmentCodeSet)) {
                        break;
                    }
                } catch (ApiException e) {
                    if (e.getCode() == 429) {
                        applyRateLimitBackoff(requestType, limitKey);
                        // 审查问题3（intentional）：429 且指定货件号尚未命中时，回退返回已拉到的全量货件，避免整任务空跑（见类注释）
                        if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isEmpty(shipmentDetailDataMap) && CollUtil.isNotEmpty(allShipmentDetailDataMap)) {
                            log.warn("【FBA入库计划货件详情拉取】platformShopCode={},存在429等待恢复且未命中输入货件号:回退返回当前全量数据,货件数={}",
                                    shopInfoDTO.getPlatformShopCode(),
                                    allShipmentDetailDataMap.size());
                            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(new ArrayList<>(allShipmentDetailDataMap.values()))));
                        }
                        if (manualPull) {
                            disableNextStatus(dmpResponse);
                            throw new ServiceException("手动拉取FBA货件详情失败: Amazon API 429 限流, taskId=" + dmpInputTaskEntity.getId());
                        }
                        log.warn("【FBA入库计划货件详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                        disableNextStatus(dmpResponse);
                        return Collections.emptyList();
                    }
                    if (manualPull) {
                        log.error("【FBA入库计划货件详情拉取】platformShopCode={}, taskId={}, inboundPlanId={}, shipmentId={}, Amazon API 异常",
                                shopInfoDTO.getPlatformShopCode(), dmpInputTaskEntity.getId(), inboundPlanId, shipmentRawId, e);
                        throw new ServiceException("手动拉取FBA货件详情失败，请稍后重试, inboundPlanId=" + inboundPlanId
                                + ", shipmentId=" + shipmentRawId + ", taskId=" + dmpInputTaskEntity.getId());
                    }
                    log.warn("跳过shipment，inboundPlanId={}, shipmentId={}", inboundPlanId, shipmentRawId, e);
                } catch (LWAException e) {
                    if (manualPull) {
                        log.error("【FBA入库计划货件详情拉取】platformShopCode={}, taskId={}, inboundPlanId={}, shipmentId={}, LWA 授权异常",
                                shopInfoDTO.getPlatformShopCode(), dmpInputTaskEntity.getId(), inboundPlanId, shipmentRawId, e);
                        throw new ServiceException("手动拉取FBA货件详情失败，请稍后重试, inboundPlanId=" + inboundPlanId
                                + ", shipmentId=" + shipmentRawId + ", taskId=" + dmpInputTaskEntity.getId());
                    }
                    log.warn("跳过shipment，inboundPlanId={}, shipmentId={}", inboundPlanId, shipmentRawId, e);
                }
            }
            if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isEmpty(pendingShipmentCodeSet)) {
                break;
            }
        }

        if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isNotEmpty(pendingShipmentCodeSet)) {
            // 审查问题3：未命中时若 allShipmentDetailDataMap 非空将走下方回退分支，否则 manualPull fail-fast
            log.info("【FBA入库计划货件详情拉取】platformShopCode={},输入货件号均未命中getShipment结果, shipmentCodeList={}",
                    shopInfoDTO.getPlatformShopCode(),
                    JSON.toJSONString(pendingShipmentCodeSet));
        }

        if (CollUtil.isEmpty(shipmentDetailDataMap)) {
            // 审查问题3（intentional）：shipmentCodeList 均未命中但同计划已有 getShipment 结果时回退全量落库；定时全量不走此分支
            if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isNotEmpty(allShipmentDetailDataMap)) {
                log.info("【FBA入库计划货件详情拉取】platformShopCode={},未命中输入货件号:回退全部落库,货件数={}",
                        shopInfoDTO.getPlatformShopCode(),
                        allShipmentDetailDataMap.size());
                return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(new ArrayList<>(allShipmentDetailDataMap.values()))));
            }
            if (manualPull) {
                throw new ServiceException("手动拉取FBA货件详情失败: 未拉取到任何货件, taskId=" + dmpInputTaskEntity.getId());
            }
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(new ArrayList<>(shipmentDetailDataMap.values()))));
    }

    private List<String> parseShipmentCodeList(String extendJson) {
        if (StringUtils.isBlank(extendJson)) {
            return Collections.emptyList();
        }
        JSONObject extendObj;
        try {
            extendObj = JSONObject.parseObject(extendJson);
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
        if (extendObj == null) {
            return Collections.emptyList();
        }
        JSONArray shipmentCodeArray = extendObj.getJSONArray("shipmentCodeList");
        if (CollUtil.isEmpty(shipmentCodeArray)) {
            return Collections.emptyList();
        }
        return shipmentCodeArray.stream()
                .map(String::valueOf)
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private String resolveShipmentCode(JSONObject shipmentJson, String fallbackShipmentCode) {
        String shipmentCode = StringUtils.trimToEmpty(shipmentJson.getString("shipmentConfirmationId"));
        if (StringUtils.isBlank(shipmentCode)) {
            shipmentCode = StringUtils.trimToEmpty(shipmentJson.getString("shipmentId"));
        }
        if (StringUtils.isBlank(shipmentCode)) {
            shipmentCode = StringUtils.trimToEmpty(shipmentJson.getString("fbaShipmentId"));
        }
        if (StringUtils.isBlank(shipmentCode)) {
            return fallbackShipmentCode;
        }
        return shipmentCode;
    }

    @SuppressWarnings("unchecked")
    private Set<String> parseShipmentRawIdSet(Map<String, Object> parentMongo) {
        Object shipmentsObj = parentMongo.get("shipments");
        if (!(shipmentsObj instanceof List)) {
            return Collections.emptySet();
        }
        Set<String> shipmentRawIdSet = new LinkedHashSet<>();
        for (Object shipmentObj : (List<Object>) shipmentsObj) {
            if (!(shipmentObj instanceof Map)) {
                continue;
            }
            Object shipmentIdObj = ((Map<String, Object>) shipmentObj).get("shipmentId");
            if (shipmentIdObj != null && StringUtils.isNotBlank(shipmentIdObj.toString())) {
                shipmentRawIdSet.add(shipmentIdObj.toString());
            }
        }
        return shipmentRawIdSet;
    }

    @SuppressWarnings("unchecked")
    private String resolveParentMarketplaceId(Map<String, Object> parentMongo) {
        Object marketplaceIdObj = parentMongo.get("marketplaceId");
        if (marketplaceIdObj != null && StringUtils.isNotBlank(marketplaceIdObj.toString())) {
            return marketplaceIdObj.toString();
        }
        Object marketplaceIdsObj = parentMongo.get("marketplaceIds");
        if (marketplaceIdsObj instanceof List && CollUtil.isNotEmpty((List<Object>) marketplaceIdsObj)) {
            Object first = ((List<Object>) marketplaceIdsObj).get(0);
            if (first != null && StringUtils.isNotBlank(first.toString())) {
                return first.toString();
            }
        }
        return "";
    }
}
