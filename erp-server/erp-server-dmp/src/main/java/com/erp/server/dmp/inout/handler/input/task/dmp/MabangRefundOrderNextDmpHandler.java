package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.server.dmp.utils.MapCountUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 退款单详情字段映射转换
 */
@Service
@Scope("prototype")
public class MabangRefundOrderNextDmpHandler extends MabangRefundOrderGetDetailDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		detailList.forEach(d -> {
			d.put("refundplatformOrderId", dmpInputMongoEntity.get("refundplatformOrderId"));
		});
		return detailList;
	}

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {

		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			HashMap<String, Integer> skuCountMap = new HashMap<>();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				Object refundStock = dmpDataMap.get("refundStock");
				Object refundplatformOrderId = dmpDataMap.get("refundplatformOrderId");
				if (refundStock != null) {
					//erp平台商品id
					String erpOrderItemId = refundplatformOrderId + "_" + refundStock;
					erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, String.valueOf(refundStock), erpOrderItemId);
					dmpDataMap.put("thirdDetailId", erpOrderItemId);
				}
			}
		}
	}
}
