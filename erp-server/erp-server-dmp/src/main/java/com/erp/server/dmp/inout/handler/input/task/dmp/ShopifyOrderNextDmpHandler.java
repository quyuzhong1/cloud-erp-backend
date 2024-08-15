package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
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
public class ShopifyOrderNextDmpHandler extends ShopifyOrderGetDetailDmpHandler {

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				//商品售价(折扣后单价)
				Object priceObj = dmpDataMap.get("price");
				Object totalDiscountObj = dmpDataMap.get("totalDiscount");
				Object quantityObj = dmpDataMap.get("quantity");

				BigDecimal price = BigDecimal.ZERO;
				BigDecimal totalDiscount = BigDecimal.ZERO;
				BigDecimal quantity = BigDecimal.ZERO;
				if (priceObj != null) {
					price = MathUtil.valueOf(priceObj);
					//折扣后订单总金额
					dmpDataMap.put("afterAmount", price.multiply(quantity));
				}
				if (totalDiscountObj != null) {
					totalDiscount = MathUtil.valueOf(totalDiscountObj);
				}
				if (quantityObj != null) {
					//商品售价(折扣后单价)
					quantity = MathUtil.valueOf(quantityObj);
					dmpDataMap.put("sellPrice", price.subtract(totalDiscount.divide(quantity, 4, BigDecimal.ROUND_HALF_UP)));
				}
			}
		}
	}
}
