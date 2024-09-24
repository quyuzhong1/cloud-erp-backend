package com.erp.server.dmp.inout.handler.output.create;

import cn.hutool.core.collection.CollUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputChildCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.service.DmpOutputTaskService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输出创建子类任务处理器
 *
 */
@Slf4j
@Service
public class DmpOutputChildCreateHandler extends DmpOutputBaseCreateHandler {

	@Resource
	private DmpOutputTaskService dmpOutputTaskService;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest, DmpOutputCreateResponse dmpResponse) {
		DmpOutputChildCreateRequest dmpOutputChildCreateRequest = (DmpOutputChildCreateRequest) dmpRequest;
		boolean throwException = dmpRequest.isThrowException();
		String parentOutputTaskId = dmpOutputChildCreateRequest.getParentOutputTaskId();
		String msg = "";
		if(StringUtils.isBlank(parentOutputTaskId)) {
			msg = "子类任务父任务id不能为空";
			log.error(msg);
			if(throwException) {
				throw new ServiceException(msg);
			}
		}
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
		String cfgOutputId = dmpCfgOutputEntity.getId();
		List<String> nextLevelIdList = dmpOutputChildCreateRequest.getNextLevelIdList();
		List<DmpOutputTaskEntity> saveDmpOutputTaskEntityList = new ArrayList<>();
		List<DmpOutputTaskEntity> allDmpOutputTaskEntityList = new ArrayList<>();
		
		if(CollUtil.isNotEmpty(nextLevelIdList)) {
			List<List<String>> partition = Lists.partition(nextLevelIdList, 10000);
			for(List<String> p : partition) {
				Map<String, DmpOutputTaskEntity> nextIdTaskEntityMap = dmpOutputTaskService.lambdaQuery()
//						.eq(DmpOutputTaskEntity::getParentTaskId, parentOutputTaskId)
//						.eq(DmpOutputTaskEntity::getTaskType, DmpOutputTaskTypeEnum.CHILD.getCode())
						.in(DmpOutputTaskEntity::getNextLevelId, p)
						.list().stream().collect(Collectors.toMap(DmpOutputTaskEntity::getNextLevelId, d -> d , (d1 , d2) -> d1));
					DmpOutputTaskEntity dmpOutputTaskEntity = null;
					for(String nextLevelId : p) {
						dmpOutputTaskEntity = nextIdTaskEntityMap.get(nextLevelId);
						if(dmpOutputTaskEntity == null) {
							dmpOutputTaskEntity = new DmpOutputTaskEntity();
							dmpOutputTaskEntity.setCfgOutputId(cfgOutputId);
							dmpOutputTaskEntity.setNextLevelId(nextLevelId);
							
							dmpOutputTaskEntity.setStartTime(dmpOutputChildCreateRequest.getStartTime());
							dmpOutputTaskEntity.setEndTime(dmpOutputChildCreateRequest.getEndTime());
							dmpOutputTaskEntity.setStatus(DmpOutputTaskStatusEnum.INIT.getCode());
//							dmpOutputTaskEntity.setTaskType(DmpOutputTaskTypeEnum.CHILD.getCode());
//							dmpOutputTaskEntity.setParentTaskId(parentOutputTaskId);
							saveDmpOutputTaskEntityList.add(dmpOutputTaskEntity);
						}
					}
					allDmpOutputTaskEntityList.addAll(nextIdTaskEntityMap.values());
			}
		}
		
		if(CollUtil.isNotEmpty(saveDmpOutputTaskEntityList)) {
			dmpOutputTaskService.saveBatch(saveDmpOutputTaskEntityList);
			allDmpOutputTaskEntityList.addAll(saveDmpOutputTaskEntityList);
		}
		
		return allDmpOutputTaskEntityList;
	}

}
