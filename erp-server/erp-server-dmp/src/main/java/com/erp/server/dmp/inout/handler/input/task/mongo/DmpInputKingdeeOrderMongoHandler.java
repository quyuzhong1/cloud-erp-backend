package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.service.DmpCfgInputDetailService;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpInputKingdeeOrderMongoHandler extends DmpInputDbConvertMongoHandler{
	
	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpInputCreateFactory dmpInputCreateFactory;
	
	@Override
	protected void afterToDo(DmpInputMongoRequest dmpRequest, DmpInputFdsResponse dmpResponse,
			List<Map> resultDmpInputMongoEntityList) {
		if(CollUtil.isNotEmpty(resultDmpInputMongoEntityList)) {
			List<String> mongoIdList = resultDmpInputMongoEntityList.stream().map(m -> m.get(MONGO_BASE_ID).toString()).collect(Collectors.toList());
			String nextCfgInputId = "1801574477567135747";
			Map<String, DmpCfgInputDetailEntity> nextLevelIdEntityMaps = dmpCfgInputDetailService.lambdaQuery()
					.eq(DmpCfgInputDetailEntity::getMainId, nextCfgInputId)
					.in(DmpCfgInputDetailEntity::getNextLevelId, mongoIdList)
					.list().stream().collect(Collectors.toMap(DmpCfgInputDetailEntity::getNextLevelId, d -> d));
			List<DmpCfgInputDetailEntity> saveDmpCfgInputDetailEntityList = new ArrayList<>();
			DmpCfgInputDetailEntity dmpCfgInputDetailEntity = null;
			
			List<String> cfgInputDetailIdList = new ArrayList<>();
			for(String mongoId : mongoIdList) {
				dmpCfgInputDetailEntity = nextLevelIdEntityMaps.get(mongoId);
				if(dmpCfgInputDetailEntity == null) {
					dmpCfgInputDetailEntity = new DmpCfgInputDetailEntity();
					dmpCfgInputDetailEntity.setMainId(nextCfgInputId);
					dmpCfgInputDetailEntity.setNextLevelId(mongoId);
					saveDmpCfgInputDetailEntityList.add(dmpCfgInputDetailEntity);
				}else {
					cfgInputDetailIdList.add(dmpCfgInputDetailEntity.getId());
				}
			}
			if(CollUtil.isNotEmpty(saveDmpCfgInputDetailEntityList)) {
				dmpCfgInputDetailService.saveBatch(saveDmpCfgInputDetailEntityList);
				cfgInputDetailIdList.addAll(saveDmpCfgInputDetailEntityList.stream().map(DmpCfgInputDetailEntity::getId).collect(Collectors.toList()));
			}
			
			DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
	    	dmpInputHotfixCreateRequest.setCfgInputId(nextCfgInputId);
	    	dmpInputHotfixCreateRequest.setCfgInputDetailIdList(cfgInputDetailIdList);
	    	dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
		}
		super.afterToDo(dmpRequest, dmpResponse, resultDmpInputMongoEntityList);
	}
}
