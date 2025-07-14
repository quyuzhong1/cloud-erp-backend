package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class WeiShiInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoData = mongoDataMaps.get(0);
		    if(mongoData.containsKey("blQualifiedProduct") && Objects.nonNull(mongoData.get("blQualifiedProduct"))){
				Boolean blQualifiedProduct = (Boolean) mongoData.get("blQualifiedProduct");
				if(blQualifiedProduct){
					for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
						dmpDataMap.put("unsellable", 0);
					}
				}else{
					for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
						dmpDataMap.put("unsellable", dmpDataMap.get("totalQty"));
						dmpDataMap.put("waitOutBoundQty", 0);
						dmpDataMap.put("availableQty",0);
						dmpDataMap.put("frozenQty", 0);
					}
				}
			}
		}
	}
}
