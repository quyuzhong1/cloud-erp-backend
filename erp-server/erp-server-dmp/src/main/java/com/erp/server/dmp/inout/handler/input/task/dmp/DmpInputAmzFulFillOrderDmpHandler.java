package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Address;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.LabelPrepType;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.FulfillmentOrder;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.FulfillmentShipment;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.FulfillmentShipmentPackageList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzFulFillOrderDmpHandler extends DmpInputDbConvertDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        // 打平原始数据
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object orderObj = mongoData.get("fulfillmentOrder");
                if (null != orderObj) {
                    FulfillmentOrder fulfillmentOrder = JSON.parseObject(JSONObject.toJSONString(orderObj), FulfillmentOrder.class);
                    dmpDataMap.put("platformCode", fulfillmentOrder.getDisplayableOrderId());
                    dmpDataMap.put("orderStatus", fulfillmentOrder.getFulfillmentOrderStatus());
                }
                Object fulfillmentShipments = dmpDataMap.get("fulfillmentShipments");
                if (null != fulfillmentShipments) {
                    List<FulfillmentShipment> fulfillmentShipmentList = JSON.parseArray(JSONObject.toJSONString(fulfillmentShipments), FulfillmentShipment.class);
                    if (CollUtil.isNotEmpty(fulfillmentShipmentList)){
                        FulfillmentShipment fulfillmentShipment = fulfillmentShipmentList.get(0);
                        dmpDataMap.put("shipmentId", fulfillmentShipment.getAmazonShipmentId());
                        dmpDataMap.put("deliveryStatus", fulfillmentShipment.getFulfillmentShipmentStatus().getValue());
                        if (Objects.nonNull(fulfillmentShipment.getShippingDate())){
                            dmpDataMap.put("deliveryTime", fulfillmentShipment.getShippingDate().toZonedDateTime());
                        }
                        FulfillmentShipmentPackageList fulfillmentShipmentPackage = fulfillmentShipment.getFulfillmentShipmentPackage();
                        if (CollUtil.isNotEmpty(fulfillmentShipmentPackage)){
                            dmpDataMap.put("trackNo", fulfillmentShipmentPackage.get(0).getTrackingNumber());
                            dmpDataMap.put("channelCode", fulfillmentShipmentPackage.get(0).getCarrierCode());
                        }
                    }
                }
                dmpDataMap.put("orderType", "soMultiChannel");
            }
        }
    }

}
