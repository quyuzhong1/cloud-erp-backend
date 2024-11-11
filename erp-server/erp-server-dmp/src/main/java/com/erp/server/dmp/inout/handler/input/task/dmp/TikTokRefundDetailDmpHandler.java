package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokRefundDetailDmpHandler extends TikTokRefundGetDetailDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        detailList.forEach(d -> {
            d.put("orderId", dmpInputMongoEntity.get("orderId"));
            d.put("returnReasonText", dmpInputMongoEntity.get("returnReasonText"));
        });
        return detailList;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            Map<String, Object> data = new HashMap<>();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object refundAmountObj = dmpDataMap.get("refundAmount");
                if (refundAmountObj != null) {
                    Map<String, Object> refundAmountMap = (Map<String, Object>) refundAmountObj;
                    data.put("refundShippingFee", refundAmountMap.get("refundShippingFee"));
                    data.put("sellPrice", refundAmountMap.get("refundSubtotal"));
                    dmpDataMap.put("extendData", JSON.toJSONString(data));
                    dmpDataMap.put("amount", refundAmountMap.get("refundTotal"));
                    dmpDataMap.put("taxAmount", refundAmountMap.get("refundTax"));
                    dmpDataMap.put("srcOrderDetailId", refundAmountMap.get("orderLineItemId"));

                    dmpDataMap.put("thirdOrderCode", dmpDataMap.get("orderId"));
                    dmpDataMap.put("platformOrderCode", dmpDataMap.get("orderId"));

                }
            }
        }
    }
}
