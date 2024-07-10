package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtNextDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
		String parentStorageName = mainConvertId.getStorageName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get(StrUtils.underlineByhump(mainConvertId.getUniqueFieldName())).toString(), listMap.get(BaseEntity.ID).toString());
			}
		}
		
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		Map<String, List<String>> originaConvertMap = new HashMap<>();
		
		String mainId = StrUtils.underlineToCamel(MAIN_ID, true);
		for(Map<String, Object> dmpInputMongoEntity : dmpInputMongoEntityList) {
			String orderNo = dmpInputMongoEntity.get("order_no").toString();
			String mainIdValue = billNoIdMap.get(orderNo);
			if(StringUtils.isBlank(mainIdValue)) {
				continue;
			}
			ArrayList<Map<String, Object>> keyList = new ArrayList<>();
			keyList.add(dmpInputMongoEntity);
			List<Object> detailsList = (List<Object>) dmpInputMongoEntity.get("details_list");
			ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();
			for(Object details : detailsList) {
				JSONObject jsonObject = (JSONObject)details;
				Set<Entry<String, Object>> entrySet = jsonObject.entrySet();
				TreeMap<String , Object> dmpInputDmpBaseEntity = new TreeMap<>();
				for (Map.Entry<String, Object> entry : entrySet) {
					String originalKey = entry.getKey().toString();
					List<String> convertKey = originaConvertMap.get(originalKey);
					if(convertKey == null) {
						convertKey = this.convertKey(originalKey);
						originaConvertMap.put(originalKey, convertKey);
					}
					for(String c : convertKey) {
						dmpInputDmpBaseEntity.put(c, entry.getValue());
					}
				}
				dmpInputDmpBaseEntity.put(mainId, mainIdValue);
				valueList.add(dmpInputDmpBaseEntity);
				
			}
			dmpInputDataDmpRelationMaps.put(keyList, valueList);
		}
		
		return dmpInputDataDmpRelationMaps;
	}
	
	
}
