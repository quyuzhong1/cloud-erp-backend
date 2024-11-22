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
public class TikTokReturnDetailDmpHandler extends TikTokReturnGetDetailDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
        List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
        detailList.forEach(d -> {
            d.put("orderId", dmpInputMongoEntity.get("orderId"));
            d.put("returnReasonText", dmpInputMongoEntity.get("returnReasonText"));
            d.put("returnType", dmpInputMongoEntity.get("returnType"));
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
                    data.put("refundTax", refundAmountMap.get("refundTax"));
                    data.put("refundShippingFee", refundAmountMap.get("refundShippingFee"));
                    dmpDataMap.put("extendData", JSON.toJSONString(data));
                    dmpDataMap.put("logisticsFeeAmount", refundAmountMap.get("refundShippingFee"));
                    dmpDataMap.put("refundTax", refundAmountMap.get("refundTax"));
                    dmpDataMap.put("sellPrice", refundAmountMap.get("refundSubtotal"));
                    dmpDataMap.put("amount", refundAmountMap.get("refundTotal"));
                    dmpDataMap.put("thirdOrderCode", dmpDataMap.get("orderId"));
                    dmpDataMap.put("platformOrderCode", dmpDataMap.get("orderId"));
                    dmpDataMap.put("soEntryId", refundAmountMap.get("orderLineItemId"));
                }



                Object returnReasonObj = dmpDataMap.get("returnReasonText");
                if (returnReasonObj != null) {
                    dmpDataMap.put("reason", returnReasonObj);
                }

                Object returnTypeObj = dmpDataMap.get("returnType");
                if (returnTypeObj != null) {
                    if ("RETURN_AND_REFUND".equals(returnTypeObj.toString())) {
                        dmpDataMap.put("solutionType", dmpDataMap.get("return_and_refund"));
                    } else if ("REFUND".equals(returnTypeObj.toString())) {
                        dmpDataMap.put("solutionType", dmpDataMap.get("refund"));
                    } else if ("REPLACEMENT".equals(returnTypeObj.toString())) {
                        dmpDataMap.put("solutionType", dmpDataMap.get("replacement"));
                    }
                }
            }
        }
    }
}
