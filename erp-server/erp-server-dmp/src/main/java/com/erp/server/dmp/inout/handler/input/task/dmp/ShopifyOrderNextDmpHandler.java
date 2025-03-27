package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.utils.MathUtil;
import com.erp.server.dmp.utils.MapCountUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
				Object priceObj = dmpDataMap.get("sellPriceOrigin");
				Object totalDiscountObj = dmpDataMap.get("discountAmount");
				Object quantityObj = dmpDataMap.get("qty");

				BigDecimal price = null == priceObj ? BigDecimal.ZERO : new BigDecimal(priceObj.toString());
				BigDecimal totalDiscount = BigDecimal.ZERO;
				BigDecimal quantity = BigDecimal.ZERO;
				if (totalDiscountObj != null) {
					totalDiscount = MathUtil.valueOf(totalDiscountObj);

					dmpDataMap.put("discountAmount", MathUtil.valueOf(totalDiscountObj));
				}
				if (quantityObj != null) {
					quantity = MathUtil.valueOf(quantityObj);
				}

				if (quantity.compareTo(BigDecimal.ZERO) > 0) {
					price = price.subtract(totalDiscount.divide(quantity, 4, RoundingMode.DOWN));
				} else {
					price = price.subtract(totalDiscount);
				}

				if (quantityObj != null) {
					//商品售价(折扣后单价)
					dmpDataMap.put("sellPrice", price);
				}
				if (priceObj != null) {
					//折扣后订单总金额
					dmpDataMap.put("afterAmount", price.multiply(quantity));
				}

			}
		}
	}
}
