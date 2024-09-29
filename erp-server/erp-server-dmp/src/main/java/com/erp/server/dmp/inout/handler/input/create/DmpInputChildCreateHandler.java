package com.erp.server.dmp.inout.handler.input.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputChildCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建子类任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputChildCreateHandler extends DmpInputBaseCreateHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpInputTaskEntity> createInputTask(DmpInputCreateRequest dmpRequest, DmpInputCreateResponse dmpResponse) {
		DmpInputChildCreateRequest dmpInputChildCreateRequest = (DmpInputChildCreateRequest)dmpRequest;
		boolean throwException = dmpRequest.isThrowException();
		String parentInputTaskId = dmpInputChildCreateRequest.getParentInputTaskId();
		String msg = "";
		if(StringUtils.isBlank(parentInputTaskId)) {
			msg = "子类任务父任务id不能为空" + parentInputTaskId;
			log.error(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
		}
		
		DmpInputTaskEntity parentEntity = dmpInputTaskService.getById(parentInputTaskId);
		if(parentEntity == null) {
			msg = "父任务不存在" + parentInputTaskId;
			log.error(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
		}
		String parentNextLevel = parentEntity.getNextLevelId();
		
		DmpCfgInputEntity dmpCfgInputEntity = dmpResponse.getDmpCfgInputEntity();
		String cfgInputId = dmpCfgInputEntity.getId();
		List<String> nextLevelIdList = dmpInputChildCreateRequest.getNextLevelIdList();
		List<DmpInputTaskEntity> saveDmpInputTaskEntityList = new ArrayList<>();
		List<DmpInputTaskEntity> allDmpInputTaskEntityList = new ArrayList<>();
		
		if(CollUtil.isNotEmpty(nextLevelIdList)) {
			List<List<String>> partition = Lists.partition(nextLevelIdList, 10000);
			for(List<String> p : partition) {
				Map<String, DmpInputTaskEntity> nextIdTaskEntityMap = dmpInputTaskService.lambdaQuery()
						.eq(DmpInputTaskEntity::getCfgInputId, cfgInputId)
						.eq(DmpInputTaskEntity::getParentTaskId, parentInputTaskId)
						.eq(DmpInputTaskEntity::getTaskType, DmpInputTaskTaskTypeEnum.CHILD.getCode())
						.in(p.stream().anyMatch(d -> StringUtils.isNotBlank(d)) , DmpInputTaskEntity::getNextLevelId, p)
						.list().stream().collect(Collectors.toMap(DmpInputTaskEntity::getNextLevelId, d -> d , (d1 , d2) -> d1));
					DmpInputTaskEntity dmpInputTaskEntity = null;
					for(String nextLevelId : p) {
						if(StringUtils.isNotBlank(nextLevelId)) {
							dmpInputTaskEntity = nextIdTaskEntityMap.get(nextLevelId);
						}else {
							dmpInputTaskEntity = nextIdTaskEntityMap.get(parentNextLevel);
						}
						if(dmpInputTaskEntity == null) {
							dmpInputTaskEntity = new DmpInputTaskEntity();
							dmpInputTaskEntity.setCfgInputId(cfgInputId);
							if(StringUtils.isBlank(nextLevelId) && StringUtils.isNotBlank(parentNextLevel)) {
								dmpInputTaskEntity.setNextLevelId(parentNextLevel);
							}else {
								dmpInputTaskEntity.setNextLevelId(nextLevelId);
							}
							
							dmpInputTaskEntity.setStartTime(dmpInputChildCreateRequest.getStartTime());
							dmpInputTaskEntity.setEndTime(dmpInputChildCreateRequest.getEndTime());
							dmpInputTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
							dmpInputTaskEntity.setTaskType(DmpInputTaskTaskTypeEnum.CHILD.getCode());
							dmpInputTaskEntity.setParentTaskId(parentInputTaskId);
							saveDmpInputTaskEntityList.add(dmpInputTaskEntity);
						}
					}
					allDmpInputTaskEntityList.addAll(nextIdTaskEntityMap.values());
			}
		}
		
		if(CollUtil.isNotEmpty(saveDmpInputTaskEntityList)) {
			dmpInputTaskService.saveBatch(saveDmpInputTaskEntityList);
			allDmpInputTaskEntityList.addAll(saveDmpInputTaskEntityList);
		}
		
		return allDmpInputTaskEntityList;
	}

}
