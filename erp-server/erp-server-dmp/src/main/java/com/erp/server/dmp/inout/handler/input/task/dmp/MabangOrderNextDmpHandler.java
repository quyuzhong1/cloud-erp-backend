package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class MabangOrderNextDmpHandler extends MabangOrderGetDetailDmpHandler {
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				//销售平台
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

				//销售平台
/*				Object stockSku = dmpDataMap.get("stockSku");
				if (stockSku != null) {
					//是否是赠品 1是 2否
					Integer returnedStatus = Integer.valueOf(stockSku + "");
					if (returnedStatus == 1) {
						dmpDataMap.put("isGift", Boolean.TRUE);
					} else {
						dmpDataMap.put("isGift", Boolean.FALSE);
					}
				}*/
			}
		}
	}
}
