package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.enums.WdtSourcePlatformEnum;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtOrderDmpHandler extends DmpInputWdtDmpHandler{

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				Object status = dmpDataMap.get("status");
				if(status != null) {
					dmpDataMap.put("trade_status", "110".equals(status.toString()) ? "approve" : status);
				}
				Object platform_id = dmpDataMap.get("sourcePlatform");
				if(platform_id != null) {
					int abs_platform_id = Math.abs(Integer.parseInt(platform_id.toString()));
					WdtSourcePlatformEnum wdtSourcePlatformEnum = WdtSourcePlatformEnum.getByCode(Integer.toString(abs_platform_id));
					if(wdtSourcePlatformEnum != null) {
						dmpDataMap.put("sourcePlatform", wdtSourcePlatformEnum.getName());
					}
				}
			}
		}
	}
	
}
