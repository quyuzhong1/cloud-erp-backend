package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputDoNextDmpHandler extends DmpInputDbConvertDmpHandler{
	
	protected DmpCfgInputConvertEntity parentDmpCfgInputConvertEntity;
	protected ServiceImpl parentServiceImpl;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		parentDmpCfgInputConvertEntity = this.getMainConvertId();
		parentServiceImpl = this.getServiceImpl(parentDmpCfgInputConvertEntity.getStorageName());
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> uniqueFieldIdMap = new HashMap<>();
		List<String> parentUniqueFieldList = new ArrayList<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			Set<String> parentUniqueFieldSet = new HashSet<>(); 
			DmpHandlerUtils.getAllFieldFlag(parentDmpCfgInputConvertEntity.getUniqueFieldName(), parentUniqueFieldSet);
			parentUniqueFieldList = new ArrayList<>(parentUniqueFieldSet);
			List<String> unParentUniqueFieldList = new ArrayList<>();
			for(String parentUniqueField: parentUniqueFieldList) {
				unParentUniqueFieldList.add(StrUtils.underlineByhump(parentUniqueField));
			}
			for(Map<String, Object> listMap : listMaps) {
				StringBuilder keySb = new StringBuilder();
				for(String parentUniqueField : unParentUniqueFieldList) {
					keySb.append(listMap.get(parentUniqueField).toString());
					keySb.append("_");
				}
				uniqueFieldIdMap.put(keySb.toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		Map<String, List<String>> originaConvertMap = new HashMap<>();
		
		String mainId = StrUtils.underlineToCamel(MAIN_ID, true);
		
		Map<String, List<String>> dmpCfgInputConvertMapping = dmpHandlerCache.getDmpCfgInputConvertMapping(parentDmpCfgInputConvertEntity.getId());
		Map<String, String> convertOrgMaps = new HashMap<>();
		for(Map.Entry<String, List<String>> dmpCfgInputConvert : dmpCfgInputConvertMapping.entrySet()) {
			List<String> value = dmpCfgInputConvert.getValue();
			for(String v : value) {
				convertOrgMaps.put(v, dmpCfgInputConvert.getKey());
			}
		}
		
		JSONObject fixedValue = new JSONObject();
		String fixedValueJson = parentDmpCfgInputConvertEntity.getFixedValueJson();
		if(StringUtils.isNotBlank(fixedValueJson)) {
			fixedValue = JSON.parseObject(fixedValueJson);
		}
		for(Map<String, Object> dmpInputMongoEntity : dmpInputMongoEntityList) {
			StringBuilder keySb = new StringBuilder();
			for(String parentUniqueField : parentUniqueFieldList) {
				String convertOrg = convertOrgMaps.get(parentUniqueField);
				if(StringUtils.isNotBlank(convertOrg)) {
					keySb.append(dmpInputMongoEntity.get(convertOrg));
				}else {
					keySb.append(fixedValue.get(parentUniqueField));
				}
				keySb.append("_");
			}
			String mainIdValue = uniqueFieldIdMap.get(keySb.toString());
			if(StringUtils.isBlank(mainIdValue)) {
				continue;
			}
			ArrayList<Map<String, Object>> keyList = new ArrayList<>();
			keyList.add(dmpInputMongoEntity);
			List<Map<String, Object>> detailsList = this.getDetailList(dmpInputMongoEntity);
			ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();
			for(Map<String, Object> details : detailsList) {
				Set<Entry<String, Object>> entrySet = details.entrySet();
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
				this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
				valueList.add(dmpInputDmpBaseEntity);
				
			}
			dmpInputDataDmpRelationMaps.put(keyList, valueList);
		}
		this.afterConvertData(dmpInputDataDmpRelationMaps);
		return dmpInputDataDmpRelationMaps;
	}
	
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		return Arrays.asList(dmpInputMongoEntity);
	}
}
