package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
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
public class MabangSoOutstockNextDmpHandler extends MabangOrderGetDetailDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		detailList.forEach(d -> {
			d.put("refundplatformOrderId", dmpInputMongoEntity.get("refundplatformOrderId"));
		});
		return detailList;
	}

	@Override
	protected DmpCfgInputConvertEntity getMainConvertId() {
		List<DmpCfgInputConvertEntity> list = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId())
				&& d.getInputStatus().equals(dmpCfgInputConvertEntity.getInputStatus()));
		return list.stream().filter(l -> "dmp_so_outstock".equals(l.getStorageName())).findFirst().orElse(null);
	}
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			HashMap<String, Integer> skuCountMap = new HashMap<>();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				Object stockSku = dmpDataMap.get("stockSku");
				Object platformOrderId = dmpDataMap.get("platformOrderId");
				if (stockSku != null) {
					//erp平台商品id
					String erpOrderItemId = platformOrderId + "_" + stockSku;
					erpOrderItemId = MapCountUtils.getErpOrderItemId(skuCountMap, String.valueOf(stockSku), erpOrderItemId);
					dmpDataMap.put("thirdDetailId", erpOrderItemId);
				}

				Object isGift = dmpDataMap.get("isGift");
				if (isGift != null) {
					//是否是赠品 1是 2否
					Integer returnedStatus = Integer.valueOf(isGift + "");
					if (returnedStatus == 1) {
						dmpDataMap.put("isGift", Boolean.TRUE);
					} else {
						dmpDataMap.put("isGift", Boolean.FALSE);
					}
				}
			}
		}
	}
}
