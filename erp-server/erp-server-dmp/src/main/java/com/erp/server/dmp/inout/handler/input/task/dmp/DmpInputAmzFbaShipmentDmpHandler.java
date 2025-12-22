package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import com.erp.server.dmp.enums.FbaOutStockTypeEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.ShopInfoMappingService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzFbaShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {

        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> result = new HashMap<>();

        if (CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
            // 提取shipment_id列表
            List<String> shipmentIds = dmpInputMongoEntityList.stream()
                    .map(d -> d.get("shipment_id") != null ? d.get("shipment_id").toString() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(shipmentIds)) {
                // 查询关联数据
                List<ParamData> paramDataList = Collections.singletonList(
                        new ParamData("shipmentId", "shipmentId", PannoEnum.IN, shipmentIds)
                );

                List<Map<String, Object>> lxData = mongoService.findMongoData(paramDataList, "lx_fba_shipment_data");
                List<Map<String, Object>> amzData = mongoService.findMongoData(paramDataList, "amz_fba_shipment_data");

                if (CollUtil.isNotEmpty(lxData) && CollUtil.isNotEmpty(amzData)) {
                    // 按shipment_id分组
                    Map<String, Map<String, Object>> lxMap = lxData.stream()
                            .collect(Collectors.toMap(d -> d.get("shipment_id").toString(), d -> d));
                    Map<String, Map<String, Object>> amzMap = amzData.stream()
                            .collect(Collectors.toMap(d -> d.get("shipment_id").toString(), d -> d));

                    amzMap.forEach((shipmentId, amzRecord) -> {
                        if (lxMap.containsKey(shipmentId)) {
                            TreeMap<String, Object> dmpData = new TreeMap<>();
                            Map<String, Object> map = lxMap.get("shipmentId");
                            int isSta = (int)map.get("is_sta");
                            dmpData.put("nextLevelId", nextLevelId);
                            dmpData.put("outStockType", isSta == 0 ? FbaOutStockTypeEnum.STA.getName() : FbaOutStockTypeEnum.AWD.getName());
                            result.put(Collections.singletonList(amzRecord), Collections.singletonList(dmpData));
                        }
                    });
                }
            }
        }
        return result;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

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
            }
        }
    }

}
