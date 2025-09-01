package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFulFillOrderDetailNextDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("fulfillmentOrderItems");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        Object platformInfoObj = dmpInputMongoEntity.get("fulfillmentOrder");
        if (null == platformInfoObj) {
            return Collections.emptyList();
        }
        JSONObject platformInfoJson = JSON.parseObject(JSON.toJSONString(platformInfoObj));
        String code = platformInfoJson.getString("sellerFulfillmentOrderId");

        Object shipmentListObj = dmpInputMongoEntity.get("fulfillmentShipments");
        JSONArray parseArray = JSON.parseArray(JSON.toJSONString(shipmentListObj));
        //合并发货详情
        JSONArray shipmentListArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(parseArray)) {
            for (Object shipmentObj : parseArray) {
                JSONObject jsonObject = (JSONObject) JSON.toJSON(shipmentObj);
                JSONArray itemArray = jsonObject.getJSONArray("fulfillmentShipmentItem");
                if (CollectionUtils.isNotEmpty(itemArray)) {
                    shipmentListArray.addAll(itemArray);
                }
            }
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object detailObj : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
            jsonObject.put("code", code);
            Integer quantity = jsonObject.getInteger("quantity");
            Integer cancelledQuantity = jsonObject.getInteger("cancelledQuantity");
            Integer unfulfillableQuantity = jsonObject.getInteger("unfulfillableQuantity");
            jsonObject.put("qty", quantity - cancelledQuantity - unfulfillableQuantity);
            //累加发货数量
            AtomicReference<Integer> shipmentQty = new AtomicReference<>(0);
            shipmentListArray.stream().filter(e -> {
                JSONObject itemJson = (JSONObject) JSON.toJSON(e);
                String itemCode = itemJson.getString("sellerFulfillmentOrderItemId");
                String sellerSku = itemJson.getString("sellerSku");
                return StringUtils.equals(itemCode, jsonObject.getString("sellerFulfillmentOrderItemId")) && StringUtils.equals(sellerSku, jsonObject.getString("sellerSku"));
            }).forEach(e -> {
                JSONObject itemJson = (JSONObject) JSON.toJSON(e);
                shipmentQty.updateAndGet(v -> v + itemJson.getInteger("quantity"));
            });
            jsonObject.put("deliveryQty", shipmentQty.get());
            resultList.add(jsonObject);
        }
        return resultList;
    }

}
