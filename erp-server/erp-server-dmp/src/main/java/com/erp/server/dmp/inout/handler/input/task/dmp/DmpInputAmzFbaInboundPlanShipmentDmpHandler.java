package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * FBA InboundPlan 货件主数据落库处理器
 */
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        if (CollUtil.isEmpty(dmpInputDataDmpRelationMaps)) {
            return;
        }

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationEntry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<Map<String, Object>> mongoDataList = relationEntry.getKey();
            if (CollUtil.isEmpty(mongoDataList)) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataList.get(0);
            List<TreeMap<String, Object>> dmpDataMaps = relationEntry.getValue();
            if (CollUtil.isEmpty(dmpDataMaps)) {
                continue;
            }
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                fillMainField(dmpDataMap, mongoData);
            }
        }
    }

    private void fillMainField(TreeMap<String, Object> dmpDataMap, Map<String, Object> mongoData) {
        String fbaShipmentId = getString(mongoData, "shipmentConfirmationId");
        if (CharSequenceUtil.isBlank(fbaShipmentId)) {
            fbaShipmentId = getString(mongoData, "fbaShipmentId");
        }
        if (CharSequenceUtil.isBlank(fbaShipmentId)) {
            fbaShipmentId = getString(mongoData, "shipmentId");
        }
        if (CharSequenceUtil.isNotBlank(fbaShipmentId)) {
            dmpDataMap.put("fbaShipmentId", fbaShipmentId);
        }

        String shipmentName = getString(mongoData, "shipmentName");
        if (CharSequenceUtil.isBlank(shipmentName)) {
            shipmentName = getString(mongoData, "name");
        }
        if (CharSequenceUtil.isNotBlank(shipmentName)) {
            dmpDataMap.put("name", shipmentName);
        }

        String shipmentStatus = getString(mongoData, "shipmentStatus");
        if (CharSequenceUtil.isBlank(shipmentStatus)) {
            shipmentStatus = getString(mongoData, "status");
        }
        if (CharSequenceUtil.isNotBlank(shipmentStatus)) {
            dmpDataMap.put("platformShipmentStatus", shipmentStatus);
        }

        String referenceId = getString(mongoData, "amazonReferenceId");
        if (CharSequenceUtil.isBlank(referenceId)) {
            referenceId = getString(mongoData, "inboundPlanId");
        }
        if (CharSequenceUtil.isNotBlank(referenceId)) {
            dmpDataMap.put("referenceId", referenceId);
        }

        String fulfillmentCenter = getNestedString(mongoData, "destination", "warehouseId");
        if (CharSequenceUtil.isNotBlank(fulfillmentCenter)) {
            dmpDataMap.put("fulfillmentCenter", fulfillmentCenter);
        }

        String fulfillmentCenterCountry = getNestedString(mongoData, "destination", "address", "countryCode");
        if (CharSequenceUtil.isNotBlank(fulfillmentCenterCountry)) {
            dmpDataMap.put("fulfillmentCenterCountry", fulfillmentCenterCountry);
        }

        String marketplaceCountryCode = resolveCountryCodeByMarketplace(mongoData);
        Address sourceAddress = parseSourceAddress(mongoData);
        if (sourceAddress != null) {
            String countryCode = CharSequenceUtil.isNotBlank(marketplaceCountryCode)
                    ? marketplaceCountryCode
                    : sourceAddress.getCountryCode();
            if (CharSequenceUtil.isNotBlank(countryCode)) {
                dmpDataMap.put("countryId", countryCode);
            }
            dmpDataMap.put("deliveryFromAddress", buildFullAddress(sourceAddress));
        } else {
            if (CharSequenceUtil.isNotBlank(marketplaceCountryCode)) {
                dmpDataMap.put("countryId", marketplaceCountryCode);
            } else {
                dmpDataMap.putIfAbsent("countryId", "");
            }
            dmpDataMap.putIfAbsent("deliveryFromAddress", "");
        }

        String packType = resolvePackType(mongoData);
        if (CharSequenceUtil.isNotBlank(packType)) {
            dmpDataMap.put("packType", packType);
        } else {
            dmpDataMap.putIfAbsent("packType", "");
        }

        String labelType = resolveLabelType(mongoData);
        if (CharSequenceUtil.isNotBlank(labelType)) {
            dmpDataMap.put("labelType", labelType);
        } else {
            dmpDataMap.putIfAbsent("labelType", "");
        }

        String labelUrl = resolveLabelUrl(mongoData);
        if (CharSequenceUtil.isNotBlank(labelUrl)) {
            dmpDataMap.put("labelUrl", labelUrl);
        } else {
            dmpDataMap.putIfAbsent("labelUrl", "");
        }
        dmpDataMap.putIfAbsent("pageType", "PackageLabel_Plain_Paper");

        if (CharSequenceUtil.isNotBlank(shipmentName)) {
            dmpDataMap.put("isSta", !shipmentName.contains("ASDN"));
        } else {
            dmpDataMap.putIfAbsent("isSta", Boolean.TRUE);
        }
    }

    private Address parseSourceAddress(Map<String, Object> mongoData) {
        Object shipFromAddressObj = mongoData.get("shipFromAddress");
        if (shipFromAddressObj != null) {
            return JSONObject.parseObject(JSON.toJSONString(shipFromAddressObj), Address.class);
        }
        Object sourceObj = mongoData.get("source");
        Map<String, Object> sourceMap = toMap(sourceObj);
        if (CollUtil.isEmpty(sourceMap)) {
            return null;
        }
        Object sourceAddressObj = sourceMap.get("address");
        if (sourceAddressObj == null) {
            return null;
        }
        return JSONObject.parseObject(JSON.toJSONString(sourceAddressObj), Address.class);
    }

    private String buildFullAddress(Address address) {
        List<String> addressPartList = new ArrayList<>();
        addressPartList.add(address.getPostalCode());
        addressPartList.add(address.getCountryCode());
        addressPartList.add(address.getStateOrProvinceCode());
        addressPartList.add(address.getCity());
        addressPartList.add(address.getAddressLine1());
        addressPartList.add(address.getAddressLine2());
        addressPartList.add(address.getName());
        return addressPartList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.joining(" "));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Collections.emptyMap();
        }
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return JSONObject.parseObject(JSON.toJSONString(value), Map.class);
    }

    private String getString(Map<String, Object> source, String key) {
        if (source == null || !source.containsKey(key) || source.get(key) == null) {
            return "";
        }
        return source.get(key).toString();
    }

    private String getNestedString(Map<String, Object> source, String... keyPath) {
        Object current = source;
        for (String key : keyPath) {
            if (!(current instanceof Map)) {
                return "";
            }
            current = ((Map<?, ?>) current).get(key);
            if (current == null) {
                return "";
            }
        }
        return current.toString();
    }

    private String resolvePackType(Map<String, Object> mongoData) {
        String packType = getString(mongoData, "packType");
        if (CharSequenceUtil.isNotBlank(packType)) {
            return packType;
        }
        Object areCasesRequired = mongoData.get("areCasesRequired");
        if (areCasesRequired instanceof Boolean) {
            return (Boolean) areCasesRequired ? "原厂包装" : "混装";
        }
        return "";
    }

    private String resolveLabelType(Map<String, Object> mongoData) {
        String labelType = getString(mongoData, "labelType");
        if (CharSequenceUtil.isNotBlank(labelType)) {
            return labelType;
        }
        String labelPrepTypeValue = getString(mongoData, "labelPrepType");
        if (CharSequenceUtil.isBlank(labelPrepTypeValue)) {
            return "";
        }
        try {
            return LabelPrepType.valueOf(labelPrepTypeValue).getDesc();
        } catch (Exception ignore) {
            return labelPrepTypeValue;
        }
    }

    private String resolveLabelUrl(Map<String, Object> mongoData) {
        String labelUrl = getString(mongoData, "labelUrl");
        if (CharSequenceUtil.isNotBlank(labelUrl)) {
            return labelUrl;
        }
        labelUrl = getString(mongoData, "label_url");
        if (CharSequenceUtil.isNotBlank(labelUrl)) {
            return labelUrl;
        }
        labelUrl = getNestedString(mongoData, "label", "labelUrl");
        if (CharSequenceUtil.isNotBlank(labelUrl)) {
            return labelUrl;
        }
        return getNestedString(mongoData, "label", "label_url");
    }

    @SuppressWarnings("unchecked")
    private String resolveCountryCodeByMarketplace(Map<String, Object> mongoData) {
        String marketplaceId = getString(mongoData, "marketplaceId");
        if (CharSequenceUtil.isBlank(marketplaceId)) {
            Object marketplaceIdsObj = mongoData.get("marketplaceIds");
            if (marketplaceIdsObj instanceof List && CollUtil.isNotEmpty((List<Object>) marketplaceIdsObj)) {
                Object first = ((List<Object>) marketplaceIdsObj).get(0);
                marketplaceId = first == null ? "" : first.toString();
            }
        }
        if (CharSequenceUtil.isBlank(marketplaceId)) {
            return "";
        }
        try {
            return AmazonMarketplaceEnum.getByMarketplaceId(marketplaceId).getCountryCode();
        } catch (ServiceException ignore) {
            return "";
        }
    }
}
