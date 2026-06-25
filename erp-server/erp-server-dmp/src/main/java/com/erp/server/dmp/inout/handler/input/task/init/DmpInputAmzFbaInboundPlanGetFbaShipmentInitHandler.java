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
 * FBA InboundPlan 货件详情拉取 - 拉取FBA货件新流程
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

        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN_SHIPMENT;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (isRateLimited(limitKey)) {
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
                        // 问题2相关：429 且指定货件号尚未命中时，回退返回已拉到的全量货件，避免整任务空跑（见方法末尾同类回退逻辑注释）
                        if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isEmpty(shipmentDetailDataMap) && CollUtil.isNotEmpty(allShipmentDetailDataMap)) {
                            log.warn("【FBA入库计划货件详情拉取】platformShopCode={},存在429等待恢复且未命中输入货件号:回退返回当前全量数据,货件数={}",
                                    shopInfoDTO.getPlatformShopCode(),
                                    allShipmentDetailDataMap.size());
                            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(new ArrayList<>(allShipmentDetailDataMap.values()))));
                        }
                        log.warn("【FBA入库计划货件详情拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                        disableNextStatus(dmpResponse);
                        return Collections.emptyList();
                    }
                    log.warn("跳过shipment，inboundPlanId={}, shipmentId={}, 原因={}", inboundPlanId, shipmentRawId, e.getMessage());
                } catch (LWAException e) {
                    log.warn("跳过shipment，inboundPlanId={}, shipmentId={}, 原因={}", inboundPlanId, shipmentRawId, e.getMessage());
                }
            }
            if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isEmpty(pendingShipmentCodeSet)) {
                break;
            }
        }

        if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isNotEmpty(pendingShipmentCodeSet)) {
            log.info("【FBA入库计划货件详情拉取】platformShopCode={},输入货件号均未命中getShipment结果, shipmentCodeList={}",
                    shopInfoDTO.getPlatformShopCode(),
                    JSON.toJSONString(pendingShipmentCodeSet));
        }

        if (CollUtil.isEmpty(shipmentDetailDataMap)) {
            // 代码审查说明（问题2）：extendJson 含 shipmentCodeList 但均未命中时，回退落库同批计划下全部 getShipment 结果，
            // 避免 429/字段差异导致「指定货件号手动拉取」完全空跑；可能同步计划内其它货件，运维需知悉。
            // 定时全量（shipmentCodeList 为空）不走此分支，仅返回已收集的 shipmentDetailDataMap。
            if (CollUtil.isNotEmpty(shipmentCodeSet) && CollUtil.isNotEmpty(allShipmentDetailDataMap)) {
                log.info("【FBA入库计划货件详情拉取】platformShopCode={},未命中输入货件号:回退全部落库,货件数={}",
                        shopInfoDTO.getPlatformShopCode(),
                        allShipmentDetailDataMap.size());
                return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(new ArrayList<>(allShipmentDetailDataMap.values()))));
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
