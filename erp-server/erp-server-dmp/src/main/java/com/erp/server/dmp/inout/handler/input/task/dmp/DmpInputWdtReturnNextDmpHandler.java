package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtReturnNextDmpHandler extends DmpInputWdtNextDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		detailList.forEach(d -> {
			d.put("warehouse_name", dmpInputMongoEntity.get("warehouse_name"));
			d.put("trade_no_list", dmpInputMongoEntity.get("trade_no_list"));
			d.put("tid_list", dmpInputMongoEntity.get("tid_list"));
			d.put("reason", dmpInputMongoEntity.get("reason"));
		});
		return detailList;
	}
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				Object amount = dmpDataMap.get("amount");
				Object qty = dmpDataMap.get("qty");
				if(amount != null && qty != null) {
					BigDecimal amountBigDecimal = new BigDecimal(amount.toString());
					BigDecimal qtyBigDecimal = new BigDecimal(qty.toString());
					if(qtyBigDecimal.compareTo(BigDecimal.ZERO) != 0) {
						dmpDataMap.put("sellPrice", amountBigDecimal.divide(qtyBigDecimal , 4, RoundingMode.HALF_UP));
					}else {
						dmpDataMap.put("sellPrice", amountBigDecimal);
					}
				}
			}
		}
	}
}
