package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import com.sdk.oms.mercado.constant.MercadoConstant;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoReturnDetailDmpHandler extends DmpInputDoNextDmpHandler {

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){

        List<ParamData> orderParamList = new ArrayList<>();
        orderParamList.add(new ParamData(MercadoConstant.MONGO_BASE_FID, MercadoConstant.MONGO_BASE_FID, PannoEnum.EQ, dmpInputMongoEntity.get("resourceId")));
        List<Map<String, Object>> orderDetailMongoList = mongoService.findMongoData(orderParamList, "mercadolibre_orderDetail_data");

        Map<String, Object> map = orderDetailMongoList.get(0);
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) map.get("orderItems");


        detailList.forEach(d -> {
            d.put("thirdOrderCode", map.get("fid"));
            d.put("platformOrderCode", map.get("fid"));
            d.put("fid", dmpInputMongoEntity.get("fid"));
            d.put("currency", dmpInputMongoEntity.get("currencyId"));
        });
        return detailList;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            Map<String, Object> data = new HashMap<>();
            HashMap<String, Integer> skuCountMap = new HashMap<>();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                Object priceObj = dmpDataMap.get("fid");
                Map<String, Object> itemMap = (Map<String, Object>) dmpDataMap.get("item");
                Object skuNo = itemMap.get("sellerSku");
                if (skuNo != null) {
                    //erp平台商品id
                    String erpOrderItemId = priceObj + "_" + skuNo;
                    erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, String.valueOf(skuNo), erpOrderItemId);
                    dmpDataMap.put("thirdDetailId", erpOrderItemId);
                    dmpDataMap.put("soEntryId", erpOrderItemId);
                    dmpDataMap.put("skuId", skuNo);
                    dmpDataMap.put("skuNo", skuNo);
                }
                //退货类型暂时仅退款
                dmpDataMap.put("solutionType", "refund");

                Object unitPriceObj = dmpDataMap.get("sellPrice");
                Object quantityObj = dmpDataMap.get("qty");
                if (unitPriceObj != null && quantityObj != null) {
                    dmpDataMap.put("amount", MathUtil.valueOf(unitPriceObj).multiply(MathUtil.valueOf(quantityObj)));
                }

            }
        }
    }
}
