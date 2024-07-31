package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.utils.MathUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class ShopifySkuNextDmpHandler extends ShopifySkuGetDetailDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		detailList.forEach(d -> {
			d.put("images", dmpInputMongoEntity.get("images"));
			d.put("status", dmpInputMongoEntity.get("status"));
		});
		return detailList;
	}


	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				//商品售价(折扣后单价)
				Object priceObj = dmpDataMap.get("price");
				Object totalDiscountObj = dmpDataMap.get("totalDiscount");
				Object quantityObj = dmpDataMap.get("quantity");
				if (priceObj != null && totalDiscountObj != null && quantityObj != null) {
					BigDecimal price = MathUtil.valueOf(priceObj);
					BigDecimal totalDiscount = MathUtil.valueOf(totalDiscountObj);
					BigDecimal quantity = MathUtil.valueOf(quantityObj);
					//商品售价(折扣后单价)
					dmpDataMap.put("sellPrice", price.subtract(totalDiscount.divide(quantity, 4, BigDecimal.ROUND_HALF_UP)));
					//折扣后订单总金额
					dmpDataMap.put("afterAmount", price.multiply(quantity));
				}

				dmpDataMap.put("skuNo", dmpDataMap.get("sku"));
				dmpDataMap.put("spuId", dmpDataMap.get("productId"));
				dmpDataMap.put("name", dmpDataMap.get("title"));
				dmpDataMap.put("sellPrice", dmpDataMap.get("price"));

				//图片
				List<Map<String, Object>> imagesObj = (List<Map<String, Object>>) dmpDataMap.get("images");
				if (CollectionUtil.isNotEmpty(imagesObj)) {
					dmpDataMap.put("imageUrls", String.valueOf(imagesObj.get(0).get("source")));
				}

				//状态
				Object statusObj = dmpDataMap.get("status");
				if (statusObj != null) {
					String status = String.valueOf(statusObj);
					if ("active".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "1");
					} else if ("archived".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "3");
					} else if ("draft".equalsIgnoreCase(status)) {
						dmpDataMap.put("status", "5");
					}
				}
			}
		}
	}
}
