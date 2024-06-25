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
import com.erp.server.dmp.service.DmpCfgInputChildService;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputDoChildDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	@Autowired
	private DmpCfgInputChildService dmpCfgInputChildService;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			List<String> mongoIds = dmpInputMongoEntityList.stream().map(d -> d.get(DmpInputMongoHandler.MONGO_BASE_ID).toString()).collect(Collectors.toList());
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, PannoEnum.IN, mongoIds));
			String childMongoStorageName = this.getChildMongoStorageName();
			if(StringUtils.isNotBlank(childMongoStorageName)) {
				List<Map<String, Object>> dmpInputMongoChildEntityList = mongoService.findMongoData(paramDataList, childMongoStorageName);
				Map<String, String> mongIdDmpIdMap = dmpInputMongoDmpRelationService.lambdaQuery()
						.in(DmpInputMongoDmpRelationEntity::getMongoId, mongoIds)
						.eq(DmpInputMongoDmpRelationEntity::getIsDeleted, false)
						.eq(DmpInputMongoDmpRelationEntity::getConvertId, this.getMainConvertId())
						.list()
						.stream().collect(Collectors.toMap(DmpInputMongoDmpRelationEntity::getMongoId, DmpInputMongoDmpRelationEntity::getDmpId));
				for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
					TreeMap<String, Object> dmpInputDmpBaseEntity = new TreeMap<>();
					String mainDmpId = mongIdDmpIdMap.get(dmpInputMongoChildEntity.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
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
						dmpInputDataDmpRelationMaps.put(Collections.singletonList(dmpInputMongoChildEntity), Collections.singletonList(dmpInputDmpBaseEntity));
					}
				}
			}
		}
		return dmpInputDataDmpRelationMaps;
	}
	
	protected String getChildMongoStorageName() {
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
		String childId = dmpCfgInputChildEntity.getChildId();
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpCfgInputConvertService.lambdaQuery()
			.eq(DmpCfgInputConvertEntity::getMainId, childId)
			.eq(DmpCfgInputConvertEntity::getInputStatus, DmpInputTaskStatusEnum.MONGO.getCode())
			.list();
		if(CollUtil.isEmpty(dmpCfgInputConvertEntityList)) {
			return null;
		}
		return DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputService.getById(childId), dmpCfgInputConvertEntityList.get(0));
	}
}
