package com.erp.server.dmp.inout.handler.input.task.dmp;

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
public class DmpInputWdtSoOutStockNextDmpHandler extends DmpInputWdtNextDmpHandler{
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				for(Map.Entry<String, Object> dmpData : dmpDataMap.entrySet()) {
					String key = dmpData.getKey();
					if(key.equals("qty")) {//转换数量，去除.00
						dmpDataMap.put(key , dmpData.getValue().toString().replace(".00", ""));
						
					}
				}
			}
		}
	}
	
}
