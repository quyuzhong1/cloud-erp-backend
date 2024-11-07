package com.erp.server.dmp.inout.handler.input.task.dmp;

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
public class MercadoOrderDetailDmpHandler extends MercadoOrderGetDetailDmpHandler {


	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		detailList.forEach(d -> {
			d.put("fid", dmpInputMongoEntity.get("fid"));
		});
		return detailList;
	}

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			HashMap<String, Integer> skuCountMap = new HashMap<>();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				//商品售价(折扣后单价)
				Object priceObj = dmpDataMap.get("fid");
				Map<String, Object> itemMap = (Map<String, Object>) dmpDataMap.get("item");
				Object skuNo = itemMap.get("sellerSku");
				if (skuNo != null) {
					//erp平台商品id
					String erpOrderItemId = priceObj + "_" + skuNo;
					erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, String.valueOf(skuNo), erpOrderItemId);
					dmpDataMap.put("thirdDetailId", erpOrderItemId);
					dmpDataMap.put("platformSku", skuNo);
				}

				Object parentItemIdObj = itemMap.get("parentItemId");
				if (parentItemIdObj != null) {
					dmpDataMap.put("platformSpuNo", parentItemIdObj);
				}

				Object titleObj = itemMap.get("title");
				if (titleObj != null) {
					dmpDataMap.put("skuName", titleObj);
				}

				Object unitPriceObj = dmpDataMap.get("sellPrice");
				Object quantityObj = dmpDataMap.get("qty");
				if (unitPriceObj != null && quantityObj != null) {
					dmpDataMap.put("afterAmount", MathUtil.valueOf(unitPriceObj).multiply(MathUtil.valueOf(quantityObj)));
				}
			}
		}
	}
}
