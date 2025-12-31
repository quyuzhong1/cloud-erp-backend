package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangInBoundListDmpHandler extends DmpInputDbConvertDmpHandler{

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				// 兼容2个引用单号
				String referenceNo = dmpDataMap.getOrDefault("reference_no", "").toString();
				dmpDataMap.put("sourceCode", referenceNo);
			}
		}
	}
}
