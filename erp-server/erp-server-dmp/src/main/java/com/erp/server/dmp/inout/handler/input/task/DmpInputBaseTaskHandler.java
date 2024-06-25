package com.erp.server.dmp.inout.handler.input.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgInputHistoryEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.service.DmpCfgInputHistoryService;
import com.erp.server.dmp.service.DmpInputTaskService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入创建任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputBaseTaskHandler extends DmpInputTaskHandler{
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpCfgInputHistoryService dmpCfgInputHistoryService;
	@Resource
    private RedisTemplate<String,Object> redisTemplate;
	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputTaskRequest");
        }
        if (!(dmpResponse instanceof DmpInputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        doDmpHandler((DmpInputTaskRequest) dmpRequest, (DmpInputTaskResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		inputTaskId = dmpRequest.getInputTaskId();
		
		if(StringUtils.isBlank(inputTaskId)) {
			log.warn("输入任务id为空");
			return;
		}
		DmpInputTaskEntity dmpInputTaskEntity = dmpInputTaskService.getById(inputTaskId);
		if(dmpInputTaskEntity == null) {
			log.warn("输入任务不存在id={}" , inputTaskId);
			return;
		}
		
		String redisKey = "dmp:input:task:" + inputTaskId;
		Integer execTimeout = 3600;
		List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityList = dmpHandlerCache.getDmpCfgInputDetailEntityList((d) -> d.getMainId().equals(dmpInputTaskEntity.getCfgInputId()) 
				&& d.getNextLevelId().equals(dmpInputTaskEntity.getNextLevelId()));
		
		DmpCfgInputDetailEntity dmpCfgInputDetailEntity = null;
		if(CollUtil.isNotEmpty(dmpCfgInputDetailEntityList)) {
			dmpCfgInputDetailEntity = dmpCfgInputDetailEntityList.get(0);
			if(dmpInputTaskEntity.getTaskType().equals(DmpInputTaskTaskTypeEnum.NORMAL.getCode()) || dmpInputTaskEntity.getTaskType().equals(DmpInputTaskTaskTypeEnum.HOTFIX.getCode())) {
				if(dmpCfgInputDetailEntity.getExecTimeout() != null && dmpCfgInputDetailEntity.getExecTimeout() > 0) {
					execTimeout = dmpCfgInputDetailEntity.getExecTimeout();
				}
			}else if(dmpInputTaskEntity.getTaskType().equals(DmpInputTaskTaskTypeEnum.HISTORY.getCode())) {
				List<DmpCfgInputHistoryEntity> dmpCfgInputHistoryEntityList = dmpCfgInputHistoryService.lambdaQuery().eq(DmpCfgInputHistoryEntity::getMainId, dmpInputTaskEntity.getId()).list();
				if(CollUtil.isNotEmpty(dmpCfgInputHistoryEntityList)) {
					DmpCfgInputHistoryEntity dmpCfgInputHistoryEntity = dmpCfgInputHistoryEntityList.get(0);
					if(dmpCfgInputHistoryEntity.getExecTimeout() != null && dmpCfgInputHistoryEntity.getExecTimeout() > 0) {
						execTimeout = dmpCfgInputHistoryEntity.getExecTimeout();
					}
				}
			}
		}
		if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), execTimeout, TimeUnit.SECONDS)) {
			try {
				dmpCfgInputEntity = dmpHandlerCache.getDmpCfgInputEntityList(d -> d.getId().equals(dmpInputTaskEntity.getCfgInputId())).get(0);
				dmpBasicSystemEntity = dmpHandlerCache.getDmpBasicSystemEntityList(d -> d.getId().equals(dmpCfgInputEntity.getSystemId())).get(0);
				
				List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = new ArrayList<>();
				beforeDmpInputTaskEntityList.add(dmpInputTaskEntity);
				dmpResponse.setBeforeDmpInputTaskEntityList(beforeDmpInputTaskEntityList);
				dmpResponse.setDmpCfgInputDetailEntity(dmpCfgInputDetailEntity);
				dmpResponse.setDmpCfgInputEntity(dmpCfgInputEntity);
				dmpResponse.setDmpBasicSystemEntity(dmpBasicSystemEntity);
				
				chain.addFirstDmpHandlerList(this.getDmpCfgInputConvertEntityList(dmpRequest, dmpResponse));
				
				chain.doDmpHandler(dmpRequest, dmpResponse);
			} catch (Exception e) {
				throw e;
			}finally {
				redisTemplate.delete(redisKey);
			}
		}
	}
	
	private List<DmpHandler> getDmpCfgInputConvertEntityList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse){
		List<DmpHandler> dmpHandlerList = new ArrayList<>();
		DmpInputTaskEntity dmpInputTaskEntity = dmpResponse.getBeforeDmpInputTaskEntityList().get(0);
		String inputStatus = dmpInputTaskEntity.getStatus();
		
		if(DmpInputTaskStatusEnum.INIT.getCode().equals(inputStatus)) {
			this.addDmpHandler(dmpRequest, dmpResponse, dmpHandlerList, DmpInputTaskStatusEnum.INIT);
		}
		this.addDmpHandler(dmpRequest, dmpResponse, dmpHandlerList, dmpRequest.getDealTaskStatus());
		
		return dmpHandlerList;
	}
	
	
	private void addDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse , List<DmpHandler> dmpHandlerList , DmpInputTaskStatusEnum dmpInputTaskStatusEnum) {
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = this.getDmpCfgInputConvertEntityListByStatus(dmpResponse.getDmpCfgInputEntity().getId(), dmpInputTaskStatusEnum);
		
		int size = dmpCfgInputConvertEntityList.size();
		for(int i = 0; i < size ; i++) {
			DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
			DmpInputTaskHandler dmpHandler = this.getDmpHandlerBean(dmpCfgInputConvertEntity.getConvertClass() , DmpInputTaskHandler.class);
			dmpHandler.setConvertId(dmpCfgInputConvertEntity.getId());
			dmpHandler.setDmpCfgInputConvertEntity(dmpCfgInputConvertEntity);
			if(i == (size - 1)) {
				dmpHandler.setCurrStatusLastHandlerFlag(true);
			}
			
			dmpHandlerList.add(dmpHandler);
		}
	}
}
