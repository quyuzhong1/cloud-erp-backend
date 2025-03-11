package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.utils.MathUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokOrderDetailDmpHandler extends TikTokOrderGetDetailDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			Map<String, Object> data = new HashMap<>();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object platformDiscountObj = dmpDataMap.get("platformDiscount");
                if (platformDiscountObj != null) {
                    BigDecimal platformDiscount = MathUtil.valueOf(platformDiscountObj);
                    dmpDataMap.put("discountAmount", platformDiscount);

                    Object sellerDiscountObj = dmpDataMap.get("sellerDiscount");
                    if (sellerDiscountObj != null) {
                        dmpDataMap.put("discountAmount", platformDiscount.add(MathUtil.valueOf(sellerDiscountObj)));
                    }
                }

                Object itemTaxObj = dmpDataMap.get("itemTax");
                if (itemTaxObj != null) {
                    List<Map<String, Object>> itemTaxMap = (List<Map<String, Object>>) itemTaxObj;
                    data.put("itemTax", itemTaxMap);
					dmpDataMap.put("extendData", JSON.toJSONString(data));
					if (CollUtil.isNotEmpty(itemTaxMap)) {
					    if (ObjectUtil.isNotEmpty(itemTaxMap.get(0).get("taxType")) && "SALES_TAX".equals(itemTaxMap.get(0).get("taxType").toString())) {
                            dmpDataMap.put("taxRate", itemTaxMap.get(0).get("taxRate"));
                        }
                    }
                }
            }
        }
    }
}
