package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzFbaShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        List<Map<String, Object>> lxData = new ArrayList<>();
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
                    List<ParamData> lxParamDataList = Collections.singletonList(
                            new ParamData("shipment_id", "shipment_id", PannoEnum.IN, shipmentIds)
                    );
                    lxData.addAll(mongoService.findMongoData(lxParamDataList, "lingxing_fba_shipment_data"));
                    labelData.addAll(mongoService.findMongoData(lxParamDataList, "amazon_fba_shipment_label_data"));
                }
            }
        }

        // 打平原始数据
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object addressObj = mongoData.get("shipFromAddress");
                if (null != addressObj) {
                    Address shipFromAddress = JSON.parseObject(JSONObject.toJSONString(addressObj), Address.class);
                    dmpDataMap.put("countryId", shipFromAddress.getCountryCode());

                    String fullAddress = String.join(" ",
                            shipFromAddress.getPostalCode(),
                            shipFromAddress.getCountryCode(),
                            shipFromAddress.getStateOrProvinceCode(),
                            shipFromAddress.getCity(),
                            shipFromAddress.getAddressLine1(),
                            shipFromAddress.getName());
                    dmpDataMap.put("deliveryFromAddress", fullAddress);
                } else {
                    dmpDataMap.put("countryId", "");
                    dmpDataMap.put("deliveryFromAddress", "");
                }
                Object areCasesRequired = mongoData.get("areCasesRequired");
                if (areCasesRequired instanceof Boolean) {
                    dmpDataMap.put("packType", (Boolean) areCasesRequired ? "原厂包装" : "混装");
                } else {
                    dmpDataMap.put("packType", "");
                }
                String labelType = "";
                Object labelPrepTypeObj = mongoData.get("labelPrepType");
                if (null != labelPrepTypeObj) {
                    LabelPrepType labelPrepType = LabelPrepType.valueOf(labelPrepTypeObj.toString());
                    labelType =  labelPrepType.getDesc();
                }
                dmpDataMap.put("labelType", labelType);
                if (!lxData.isEmpty()) {
                    for (Map<String, Object> lxDatum : lxData) {
                        if (lxDatum.get("shipment_id").toString().equals(dmpDataMap.get("fbaShipmentId"))) {
                            int isSta = (int)lxDatum.get("is_sta");
                            dmpDataMap.put("isSta",isSta == 0 ? Boolean.FALSE : Boolean.TRUE);
                        }
                    }

                }
                if (!labelData.isEmpty()) {
                    for (Map<String, Object> label : labelData) {
                        if (label.get("shipment_id").toString().equals(dmpDataMap.get("fbaShipmentId"))) {
                            String labelUrl = (String)label.get("label_url");
                            dmpDataMap.put("labelUrl", CharSequenceUtil.isNotBlank(labelUrl) ? labelUrl : "");
                            dmpDataMap.put("pageType", "PackageLabel_Plain_Paper");
                        }
                    }

                }
            }
        }
    }

}
