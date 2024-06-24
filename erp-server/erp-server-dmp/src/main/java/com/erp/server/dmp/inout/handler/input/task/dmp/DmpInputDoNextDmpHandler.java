package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpInputMongoDmpRelationEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpInputMongoDmpRelationService;

@Service
@Scope("prototype")
public class DmpInputDoNextDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Autowired
	private DmpInputMongoDmpRelationService dmpInputMongoDmpRelationService;
	
	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData = super.convertData(dmpInputMongoEntityList);
		List<String> mongoIds = dmpInputMongoEntityList.stream().map(d -> d.get(DmpInputMongoHandler.MONGO_BASE_ID).toString()).collect(Collectors.toList());
		Map<String, String> mongIdDmpIdMap = dmpInputMongoDmpRelationService.lambdaQuery()
				.in(DmpInputMongoDmpRelationEntity::getMongoId, mongoIds)
				.eq(DmpInputMongoDmpRelationEntity::getIsDeleted, false)
				.eq(DmpInputMongoDmpRelationEntity::getConvertId, this.getMainConvertId())
				.list()
				.stream().collect(Collectors.toMap(DmpInputMongoDmpRelationEntity::getMongoId, DmpInputMongoDmpRelationEntity::getDmpId));
		for(List<TreeMap<String, Object>> dmpInputMongoNextEntityList : convertData.values()) {
			for(TreeMap<String, Object> dmpInputMongoNextEntity : dmpInputMongoNextEntityList) {
				String mainDmpId = mongIdDmpIdMap.get(dmpInputMongoNextEntity.get(DmpInputMongoHandler.MONGO_BASE_ID));
				dmpInputMongoNextEntity.put(StrUtils.underlineToCamel(MAIN_ID, true), mainDmpId);
				System.out.println(dmpInputMongoNextEntity);
			}
		}
		return convertData;
	}
	
	
}
