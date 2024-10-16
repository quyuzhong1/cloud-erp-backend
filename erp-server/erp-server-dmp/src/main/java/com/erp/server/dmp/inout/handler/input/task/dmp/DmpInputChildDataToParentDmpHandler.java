package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

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
public class DmpInputChildDataToParentDmpHandler extends DmpInputDbConvertDmpHandler{
	@Override
	protected void beforeConvertData(List<Map<String, Object>> dmpInputMongoEntityList) {
		String mongoStatus = DmpInputTaskStatusEnum.MONGO.getCode();
		String cfgInputId = dmpCfgInputEntity.getId();
		List<DmpCfgInputChildEntity> dmpCfgInputChildEntityList = dmpCfgInputChildService.lambdaQuery()
			.eq(DmpCfgInputChildEntity::getParentId, cfgInputId)
			.eq(DmpCfgInputChildEntity::getInputStatus, mongoStatus)
			.list();
		if(CollUtil.isEmpty(dmpCfgInputChildEntityList)) {
			return;
		}
		
		DmpCfgInputChildEntity dmpCfgInputChildEntity = dmpCfgInputChildEntityList.get(0);
		if(dmpCfgInputChildEntityList.size() >  1) {
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(cfgInputId) 
						&& d.getInputStatus().equals(DmpInputTaskStatusEnum.DMP.getCode())
						&& d.getOrder() == this.childOrder());
			if(CollUtil.isEmpty(dmpCfgInputConvertEntityList)) {
				return;
			}
			dmpCfgInputChildEntity = dmpCfgInputChildEntityList.stream()
					.filter(d -> d.getConvertId().equals(dmpCfgInputConvertEntityList.get(0).getId()))
					.findFirst()
					.orElse(null);
			if(dmpCfgInputChildEntity == null) {
				return;
			}
		}
		String childId = dmpCfgInputChildEntity.getChildId();
		DmpCfgInputEntity childDmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(childId)).get(0);
		DmpBasicSystemEntity childDmpBasicSystemEntity = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(childDmpCfgInputEntity.getSystemId())).get(0);
		DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(childId) 
				&& d.getInputStatus().equals(mongoStatus)).get(0);
		String mongoStorageName = DmpHandlerUtils.getMongoStorageName(childDmpBasicSystemEntity, childDmpCfgInputEntity, childDmpCfgInputConvertEntity);
		
		List<DmpInputTaskEntity> dmpInputTaskEntityList = dmpInputTaskService.lambdaQuery()
			.eq(DmpInputTaskEntity::getParentTaskId, inputTaskId)
			.eq(DmpInputTaskEntity::getCfgInputId, childId)
			.list();
		if(CollUtil.isEmpty(dmpInputTaskEntityList)) {
			return;
		}
		
		DmpInputTaskEntity childDmpInputTaskEntity = dmpInputTaskEntityList.get(0);
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, childDmpInputTaskEntity.getId()));
		
		List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, mongoStorageName);
		if(CollUtil.isNotEmpty(findMongoData)) {
			dmpInputMongoEntityList.clear();
			dmpInputMongoEntityList.addAll(findMongoData);
		}
	}
	
	protected int childOrder() {
		return 1;
	}
}
