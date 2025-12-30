package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.sdk.oms.amz.spapi.model.awd.Address;
import com.erp.sdk.oms.amz.spapi.model.awd.InboundShipment;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzAwdShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        List<Map<String, Object>> detailData = new ArrayList<>();
        List<Map<String, Object>> labelData = new ArrayList<>();
        if (CollUtil.isNotEmpty(dmpInputDataDmpRelationMaps)) {
            for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
                List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
                // 提取shipment_id列表
                List<String> shipmentIds = dmpDataMaps.stream()
                        .map(d -> d.get("fbaShipmentId") != null ? d.get("fbaShipmentId").toString() : null)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (CollUtil.isNotEmpty(shipmentIds)) {
                    // 查询关联数据
                    List<ParamData> paramDataList = Collections.singletonList(
                            new ParamData("shipmentId", "shipmentId", PannoEnum.IN, shipmentIds)
                    );
                    detailData.addAll(mongoService.findMongoData(paramDataList, "amazon_awd_shipment_detail_data"));
                    labelData.addAll(mongoService.findMongoData(paramDataList, "amazon_awd_shipment_label_data"));
                }
            }
        }

        // 打平原始数据
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                if (!detailData.isEmpty()) {
                    for (Map<String, Object> detail : detailData) {
                        if (detail.get("shipmentId").toString().equals(dmpDataMap.get("fbaShipmentId"))) {
                            //配送地址
                            Object destinationAddressObj = detail.get("destinationAddress");
                            Address destinationAddress = JSONObject.parseObject(JSONObject.toJSONString(destinationAddressObj), Address.class);
                            if (destinationAddress != null) {
                                String deliveryToAddress = String.join(" ", destinationAddress.getAddressLine1(), destinationAddress.getAddressLine2(), destinationAddress.getAddressLine3());
                                dmpDataMap.put("deliveryToAddress",deliveryToAddress);
                                dmpDataMap.put("deliveryToMobile", destinationAddress.getPhoneNumber());
                                dmpDataMap.put("deliveryToName", destinationAddress.getName());
                                dmpDataMap.put("deliveryToPostCode", destinationAddress.getPostalCode());
                                dmpDataMap.put("deliveryToArea", destinationAddress.getDistrict());
                                dmpDataMap.put("deliveryToProvince", destinationAddress.getStateOrRegion());
                                dmpDataMap.put("deliveryToCity", destinationAddress.getCity());
                                dmpDataMap.put("deliveryToCountryId", destinationAddress.getCountryCode());
                                dmpDataMap.put("deliveryToCountryName", destinationAddress.getCounty());
                            }
                            //发货地址
                            Object originAddressObj = detail.get("originAddress");
                            if (originAddressObj != null) {
                                Address originAddress = JSONObject.parseObject(JSONObject.toJSONString(originAddressObj), Address.class);
                                String deliveryFromAddress = String.join(" ", originAddress.getAddressLine1(), originAddress.getAddressLine2(), originAddress.getAddressLine3());
                                dmpDataMap.put("deliveryFromAddress",deliveryFromAddress);
                                dmpDataMap.put("deliveryFromMobile", originAddress.getPhoneNumber());
                                dmpDataMap.put("deliveryFromName", originAddress.getName());
                                dmpDataMap.put("deliveryFromPostCode", originAddress.getPostalCode());
                                dmpDataMap.put("deliveryFromArea", originAddress.getDistrict());
                                dmpDataMap.put("deliveryFromProvince", originAddress.getStateOrRegion());
                                dmpDataMap.put("deliveryFromCity", originAddress.getCity());
                                dmpDataMap.put("countryId", originAddress.getCountryCode());
                                dmpDataMap.put("countryName", originAddress.getCounty());
                            }
                            //基础信息
                            dmpDataMap.put("platformShipmentStatus", detail.getOrDefault("shipmentStatus",""));
                            String createdAt = (String)detail.getOrDefault("createdAt", "");
                            if (CharSequenceUtil.isNotBlank(createdAt)){
                                OffsetDateTime offsetDateTime = OffsetDateTime.parse(createdAt);
                                LocalDateTime platformCreateTime = offsetDateTime.toLocalDateTime();
                                dmpDataMap.put("platformCreateTime", platformCreateTime);
                            }
                            String updatedAt = (String)detail.getOrDefault("updatedAt", "");
                            if (CharSequenceUtil.isNotBlank(updatedAt)){
                                OffsetDateTime offsetDateTime = OffsetDateTime.parse(updatedAt);
                                LocalDateTime platformUpdateTime = offsetDateTime.toLocalDateTime();
                                dmpDataMap.put("platformUpdateTime", platformUpdateTime);
                            }
                            dmpDataMap.put("platformOrderId",detail.getOrDefault("orderId",""));
                            String shipBy = (String)detail.getOrDefault("shipBy", "");
                            if (CharSequenceUtil.isNotBlank(shipBy)){
                                OffsetDateTime offsetDateTime = OffsetDateTime.parse(shipBy);
                                LocalDateTime shipmentDeliveryTime = offsetDateTime.toLocalDateTime();
                                dmpDataMap.put("shipmentDeliveryTime", shipmentDeliveryTime);
                            }
                            dmpDataMap.put("fulfillmentCenter",detail.getOrDefault("warehouseReferenceId",""));
                        }
                    }

                }
                if (!labelData.isEmpty()) {
                    for (Map<String, Object> label : labelData) {
                        if (label.get("shipment_id").toString().equals(dmpDataMap.get("fbaShipmentId"))) {
                            String labelUrl = (String)label.get("label_url");
                            dmpDataMap.put("labelUrl", CharSequenceUtil.isNotBlank(labelUrl) ? labelUrl : "");
                            dmpDataMap.put("pageType", "PLAIN_PAPER");
                        }
                    }

                }
            }
        }
    }

}
