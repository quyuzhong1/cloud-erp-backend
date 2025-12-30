package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp处理子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputDoChildDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		this.beforeConvertData(dmpInputMongoEntityList);
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			String childMongoStorageName = this.getChildMongoStorageName();
			if(StringUtils.isNotBlank(childMongoStorageName)) {
				List<Map<String, Object>> dmpInputMongoChildEntityList = this.getDmpInputMongoChildEntityList(dmpInputMongoEntityList, childMongoStorageName);
				if(CollUtil.isNotEmpty(dmpInputMongoChildEntityList)) {
					this.putDmpId(dmpInputMongoChildEntityList);
					for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
						TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
						Object mainDmpIdObj = dmpInputMongoChildEntity.get(MAIN_ID);
						if(mainDmpIdObj == null) {
							log.error("子类数据mongo集合" + childMongoStorageName + "的id=" + dmpInputMongoChildEntity.get(DmpInputMongoHandler.MONGO_BASE_ID) + "未查询到dmpid");
							continue;
						}
						String mainDmpId = mainDmpIdObj.toString();
						if(StringUtils.isNotBlank(mainDmpId)) {
							dmpInputDmpBaseEntity.put(StrUtils.underlineToCamel(MAIN_ID, true), mainDmpId);
							Set<Entry<String, Object>> entrySet = dmpInputMongoChildEntity.entrySet();
							for (Map.Entry<String, Object> d : entrySet) {
								String key = d.getKey();
								Object value = d.getValue();
								List<String> convertKey = this.convertKey(key);
								for(String c : convertKey) {
									dmpInputDmpBaseEntity.put(c.replace(".", ""), value);
								}
							}
							this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
							dmpInputDataDmpRelationMaps.put(Collections.singletonList(dmpInputMongoChildEntity), Collections.singletonList(dmpInputDmpBaseEntity));
						}
					}
				}
			}
		}
		this.afterConvertData(dmpInputDataDmpRelationMaps);
		return dmpInputDataDmpRelationMaps;
	}
	
	protected String getDmpCfgInputChildId() {
		String cfgInputId = dmpCfgInputEntity.getId();
		List<DmpCfgInputChildEntity> dmpCfgInputChildList = dmpCfgInputChildService.lambdaQuery()
			.eq(DmpCfgInputChildEntity::getParentId, cfgInputId)
			.eq(DmpCfgInputChildEntity::getInputStatus, DmpInputTaskStatusEnum.MONGO.getCode())
			.eq(DmpCfgInputChildEntity::getConvertId, convertId)
			.list();
		if(CollUtil.isEmpty(dmpCfgInputChildList)) {
			return null;
		}
		DmpCfgInputChildEntity dmpCfgInputChildEntity = dmpCfgInputChildList.get(0);
		return dmpCfgInputChildEntity.getChildId();
	}
	
	protected String getChildMongoStorageName() {
		String childId = this.getDmpCfgInputChildId();
		if(StringUtils.isBlank(childId)) {
			return null;
		}
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(childId) 
				&& d.getInputStatus().equals(DmpInputTaskStatusEnum.MONGO.getCode()));
		if(CollUtil.isEmpty(dmpCfgInputConvertEntityList)) {
			return null;
		}
		return DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputService.getById(childId), dmpCfgInputConvertEntityList.get(0));
	}
	
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<String> mongoIds = dmpInputMongoEntityList.stream().map(d -> d.get(DmpInputMongoHandler.MONGO_BASE_ID).toString()).collect(Collectors.toList());
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.IN, mongoIds));
		List<Map<String, Object>> dmpInputMongoChildEntityList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		return dmpInputMongoChildEntityList;
	}
	
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList){
		List<String> mongoIds = dmpInputMongoChildEntityList.stream().map(d -> d.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID).toString()).collect(Collectors.toList());
		Map<String, String> mongIdDmpIdMap = dmpInputMongoDmpRelationService.lambdaQuery()
				.in(DmpInputMongoDmpRelationEntity::getMongoId, mongoIds)
				.eq(DmpInputMongoDmpRelationEntity::getIsDeleted, false)
				.eq(DmpInputMongoDmpRelationEntity::getConvertId, this.getMainConvertId().getId())
				.list()
				.stream().collect(Collectors.toMap(DmpInputMongoDmpRelationEntity::getMongoId, DmpInputMongoDmpRelationEntity::getDmpId));
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String nextLevelId = dmpInputMongoChildEntity.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID).toString();
			String dmpId = mongIdDmpIdMap.get(nextLevelId);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
