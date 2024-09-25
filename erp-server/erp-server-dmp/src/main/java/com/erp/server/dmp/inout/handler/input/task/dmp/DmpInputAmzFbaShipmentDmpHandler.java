package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
