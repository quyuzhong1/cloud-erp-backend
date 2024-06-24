package com.erp.server.dmp.inout.handler.input.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpCfgInputTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.handler.output.task.init.DmpOutputInitHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.DmpInputTaskFileService;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.erp.server.dmp.service.impl.DmpCfgInputChildServiceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入任务处理器
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpInputTaskHandler extends DmpInputHandler{
	@Autowired
	protected DmpInputTaskService dmpInputTaskService;
	@Autowired
	protected DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	protected DmpCfgOutputDetailService dmpCfgOutputDetailService;
	@Autowired
	protected DmpCfgApiService dmpCfgApiService;
	@Autowired
	protected DmpCfgInputChildServiceImpl dmpCfgInputChildServiceImpl;
	@Autowired
	protected DmpInputCreateFactory dmpInputCreateFactory;
	@Autowired
	protected DmpCfgInputConvertService dmpCfgInputConvertService;
	@Autowired
	protected DmpInputTaskFileService dmpInputTaskFileService;
	@Autowired
	protected MongoService mongoService;
	@Autowired
	protected DmpCfgInputService dmpCfgInputService;
	
	/**----------多例对象属性,初始化在DmpInputBaseTaskHandler.addDmpHandler(DmpInputTaskRequest, DmpInputTaskResponse, List<DmpHandler>, DmpInputTaskStatusEnum)-----------**/
	protected String convertId;
	protected DmpCfgInputConvertEntity dmpCfgInputConvertEntity;
	protected boolean currStatusLastHandlerFlag = false;
	
	public void setConvertId(String convertId) {
		this.convertId = convertId;
	}

	public void setDmpCfgInputConvertEntity(DmpCfgInputConvertEntity dmpCfgInputConvertEntity) {
		this.dmpCfgInputConvertEntity = dmpCfgInputConvertEntity;
	}
	
	public void setCurrStatusLastHandlerFlag(boolean currStatusLastHandlerFlag) {
		this.currStatusLastHandlerFlag = currStatusLastHandlerFlag;
	}
	/**----------多例对象属性,初始化在DmpInputBaseTaskHandler.addDmpHandler(DmpInputTaskRequest, DmpInputTaskResponse, List<DmpHandler>, DmpInputTaskStatusEnum)-----------**/
	
	/**-----------------------------公共对象初始化属性,初始化在DmpInputTaskHandler.doDmpHandler(DmpRequest, DmpResponse, DmpHandlerChain)------------------------------------**/
	protected String inputTaskId;
	protected String nextLevelId;
	protected DmpCfgInputEntity dmpCfgInputEntity;
	protected DmpBasicSystemEntity dmpBasicSystemEntity;
	protected DmpCfgInputDetailEntity dmpCfgInputDetailEntity;
	protected DmpInputTaskEntity dmpInputTaskEntity;
	protected DmpInputTaskStatusEnum updateTaskStatus;
	/**-----------------------------公共对象初始化属性,初始化在DmpInputTaskHandler.doDmpHandler(DmpRequest, DmpResponse, DmpHandlerChain)------------------------------------**/

	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputRequest");
        }
        if (!(dmpResponse instanceof DmpInputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        
        DmpInputTaskRequest dmpInputTaskRequest = (DmpInputTaskRequest) dmpRequest;
        DmpInputTaskResponse dmpInputTaskResponse = (DmpInputTaskResponse) dmpResponse;
		inputTaskId = dmpInputTaskRequest.getInputTaskId();
		List<DmpInputTaskEntity> beforeDmpInputTaskEntityList = dmpInputTaskResponse.getBeforeDmpInputTaskEntityList();
		if(CollUtil.isNotEmpty(beforeDmpInputTaskEntityList)) {
			dmpInputTaskEntity = beforeDmpInputTaskEntityList.get(0);
			nextLevelId = dmpInputTaskEntity.getNextLevelId();
		}
        
		dmpCfgInputDetailEntity = dmpInputTaskResponse.getDmpCfgInputDetailEntity();
		dmpCfgInputEntity = dmpInputTaskResponse.getDmpCfgInputEntity();
		dmpBasicSystemEntity = dmpInputTaskResponse.getDmpBasicSystemEntity();
		
		doDmpHandler(dmpInputTaskRequest, dmpInputTaskResponse, chain);
	}
	
	protected void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	protected <T extends DmpHandler> T getDmpHandlerBean(String beanClass , Class<T> clazz) {
		if(StringUtils.isBlank(beanClass)) {
			return null;
		}
		return ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(beanClass) , clazz);
	}
	
	protected List<DmpCfgInputConvertEntity> getDmpCfgInputConvertEntityListByStatus(String cfgInputId , DmpInputTaskStatusEnum dmpInputTaskStatusEnum){
		return dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getMainId, cfgInputId)
				.eq(DmpCfgInputConvertEntity::getDisabled, Boolean.FALSE)
				.eq(DmpCfgInputConvertEntity::getInputStatus, dmpInputTaskStatusEnum.getCode())
				.orderByAsc(DmpCfgInputConvertEntity::getOrder)
				.list();
	}
	
	protected List<String> getOutputClassList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<String> apiClassList = new ArrayList<>();

		List<DmpCfgOutputEntity> dmpCfgOutputEntityList = dmpCfgOutputService.lambdaQuery()
				.eq(DmpCfgOutputEntity::getInputConvertId, convertId)
				.eq(DmpCfgOutputEntity::getDisabled, Boolean.FALSE)
				.list();
		if(CollUtil.isNotEmpty(dmpCfgOutputEntityList)) {
			for(DmpCfgOutputEntity dmpCfgOutputEntity : dmpCfgOutputEntityList) {
				List<DmpCfgOutputDetailEntity> dmpCfgOutputDetailEntityList = dmpCfgOutputDetailService.lambdaQuery()
						.eq(DmpCfgOutputDetailEntity::getMainId, dmpCfgOutputEntity.getId())
						.eq(StringUtils.isNotBlank(nextLevelId) , DmpCfgOutputDetailEntity::getNextLevelId, nextLevelId)
						.list();
				for(DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity : dmpCfgOutputDetailEntityList) {
					String type = dmpCfgOutputEntity.getType();
					String apiClass = "";
					if(DmpCfgInputTypeEnum.API.getCode().equals(type)) {
						String typeId = dmpCfgOutputEntity.getTypeId();
						if(StringUtils.isNotBlank(typeId)) {
							DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(typeId);
							apiClass = dmpCfgApiEntity.getApiClass();
						}
					}else if(DmpCfgInputTypeEnum.MQ.getCode().equals(type)) {
						apiClass = "mqPushHandler";
					}else if(DmpCfgInputTypeEnum.DB.getCode().equals(type)) {
						apiClass = "dbPushHandler";
					}
					apiClassList.add(apiClass);
				}
			}
		}
	
		return apiClassList;
	}
	
	protected boolean updateTaskStatus(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		if(currStatusLastHandlerFlag) {
			this.beforeToDoUpdateTaskStatus(dmpRequest, dmpResponse);
			boolean updateSuccess = dmpInputTaskService.lambdaUpdate()
					.eq(DmpInputTaskEntity::getId, inputTaskId)
					.set(DmpInputTaskEntity::getStatus, updateTaskStatus.getCode())
					.set(DmpInputTaskEntity::getUpdateTime, LocalDateTime.now())
					.update();
			this.afterToDoUpdateTaskStatus(dmpRequest, dmpResponse, updateSuccess);
			return updateSuccess;
		}
		return true;
	}
	
	protected void doBaseChain(DmpInputTaskRequest dmpRequest, DmpInputInitResponse dmpResponse , DmpHandlerChain chain , DmpOutputRequest dmpOutputRequest) {
		if(dmpResponse.isDoOutputChain()) {
			this.doOutputChain(dmpRequest, dmpResponse, chain, dmpOutputRequest);
		}
		if(dmpResponse.isDoChildCfgInput()) {
			this.doChildCfgInput(dmpRequest, dmpResponse);
		}
		if(dmpResponse.isDoUpdateStatus()) {
			this.updateTaskStatus(dmpRequest, dmpResponse);
		}
		if(dmpResponse.isDoNextChain()) {
			this.doNextChain(dmpRequest, dmpResponse, chain);
		}
	}
	
	/**
	 * 转换key
	 * @param originalKey
	 * @return
	 */
	protected List<String> convertKey(String originalKey) {
		return Collections.singletonList(originalKey);
	}
	
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse){
		return null;
	}
	
	protected void dealConvertInputTaskFileEntityListMaps(DmpInputFdsResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = dmpResponse.getConvertInputTaskFileEntityListMaps();
		if(convertInputTaskFileEntityListMaps == null || convertInputTaskFileEntityListMaps.size() == 0) {
			convertInputTaskFileEntityListMaps = new HashMap<>();
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = this.getDmpCfgInputConvertEntityListByStatus(dmpCfgInputEntity.getId(), DmpInputTaskStatusEnum.FDS);
			List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpInputTaskFileService.lambdaQuery().eq(DmpInputTaskFileEntity::getMainId, inputTaskId).list();
			if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
				Map<String, List<DmpInputTaskFileEntity>> convertIdInputTaskFileEntityListMaps = dmpInputTaskFileEntityList.stream().collect(Collectors.groupingBy(DmpInputTaskFileEntity::getFdsConvertId));
				for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
					convertInputTaskFileEntityListMaps.put(dmpCfgInputConvertEntity, convertIdInputTaskFileEntityListMaps.get(dmpCfgInputConvertEntity.getId()));
				}
			}
		}
	}
	
	protected void dealDmpInputMongoBaseEntityList(DmpInputMongoResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> convertInputMongoEntityListMaps = dmpResponse.getConvertInputMongoEntityListMaps();
		if(convertInputMongoEntityListMaps == null || convertInputMongoEntityListMaps.size() == 0) {
			convertInputMongoEntityListMaps = new HashMap<>();
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = this.getDmpCfgInputConvertEntityListByStatus(dmpCfgInputEntity.getId(), DmpInputTaskStatusEnum.MONGO);
			for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
				List<ParamData> paramDataList = new ArrayList<>();
				paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, inputTaskId));
				String mongoStorageName = DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputEntity, dmpCfgInputConvertEntity);
				List<Map<String, Object>> dmpInputMongoBaseEntityList = mongoService.findMongoData(paramDataList, mongoStorageName);
				convertInputMongoEntityListMaps.put(dmpCfgInputConvertEntity, dmpInputMongoBaseEntityList);
			}
		}
	}
	
	protected void dealConvertInputDmpBaseEntityListMaps(DmpInputDmpResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpResponse.getConvertInputDmpBaseEntityListMaps();
		if(convertInputDmpBaseEntityListMaps == null || convertInputDmpBaseEntityListMaps.size() == 0) {
			convertInputDmpBaseEntityListMaps = new HashMap<>();
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = this.getDmpCfgInputConvertEntityListByStatus(dmpCfgInputEntity.getId(), DmpInputTaskStatusEnum.DMP);
			for(DmpCfgInputConvertEntity dmpCfgInputConvertEntity : dmpCfgInputConvertEntityList) {
				QueryWrapper<?> wrapper = new QueryWrapper<>();
				wrapper.eq(DmpInputDmpHandler.INPUT_TASK_ID, inputTaskId);
				wrapper.eq(DmpInputDmpHandler.CONVERT_ID, dmpCfgInputConvertEntity.getId());
				convertInputDmpBaseEntityListMaps.put(dmpCfgInputConvertEntity, this.getServiceImpl(dmpCfgInputConvertEntity.getStorageName()).list(wrapper));
			}
		}
	}
	
	protected ServiceImpl getServiceImpl(String storageName) {
		return ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
	}
	
	/**
	 * 处理子类任务
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param resultDmpInputMongoEntityList
	 */
	protected List<DmpInputHotfixCreateRequest> doChildCfgInput(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpInputHotfixCreateRequest> dmpInputHotfixCreateRequestList = new ArrayList<>();
		List<String> nextLevelIdList = this.getNextLevelIdList(dmpRequest, dmpResponse);
		this.beforeToDoChildCfgInput(dmpRequest, dmpResponse , nextLevelIdList);
		if(CollUtil.isNotEmpty(nextLevelIdList)) {
			List<DmpCfgInputChildEntity> cfgInputChildList = this.getCfgInputChildList(dmpRequest , dmpResponse);
			if(CollUtil.isNotEmpty(cfgInputChildList)) {
				List<String> childCfgInputIdList = cfgInputChildList.stream().map(DmpCfgInputChildEntity::getChildId).collect(Collectors.toList());
				for(String childCfgInputId : childCfgInputIdList) {
					DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
			    	dmpInputHotfixCreateRequest.setCfgInputId(childCfgInputId);
			    	dmpInputHotfixCreateRequest.setNextLevelIdList(nextLevelIdList);
			    	dmpInputHotfixCreateRequest.setParentInputTaskId(inputTaskId);
			    	dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
				}
			}
		}
		this.afterToDoChildCfgInput(dmpRequest, dmpResponse, dmpInputHotfixCreateRequestList);
		return dmpInputHotfixCreateRequestList;
	}
	
	protected void doOutputChain(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse , DmpHandlerChain chain , DmpOutputRequest dmpOutputRequest) {
		List<String> outputClassList = this.getOutputClassList(dmpRequest, dmpResponse);
		this.beforeToDoOutputChain(dmpRequest, dmpResponse , outputClassList);
		Map<String, DmpOutputInitResponse> outputResultMap = new HashMap<>();
		if(CollUtil.isNotEmpty(outputClassList)) {
			dmpOutputRequest.setDoNextChain(false);
			for(String outputClass : outputClassList) {
				DmpOutputInitHandler dmpHandlerBean = this.getDmpHandlerBean(outputClass, DmpOutputInitHandler.class);
				DmpOutputFinishResponse dmpOutputFinishResponse = new DmpOutputFinishResponse();
				dmpHandlerBean.doDmpHandler(BeanUtil.copyProperties(dmpOutputRequest, DmpOutputRequest.class), dmpOutputFinishResponse, chain);
				outputResultMap.put(outputClass, dmpOutputFinishResponse);
			}
		}
		this.afterToDoOutputChain(dmpRequest, dmpResponse , outputResultMap);
	}
	
	protected void doNextChain(DmpInputTaskRequest dmpRequest, DmpInputInitResponse dmpResponse , DmpHandlerChain chain) {
		dmpResponse.setDoOutputChain(true);
		dmpResponse.setDoChildCfgInput(true);
		dmpResponse.setDoUpdateStatus(true);
		this.beforeToDoNextChain(dmpRequest, dmpResponse);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		this.afterToDoNextChain(dmpRequest, dmpResponse);
	}
	
	protected boolean isNextStatus(DmpInputInitResponse dmpResponse) {
		String status = dmpResponse.getBeforeDmpInputTaskEntityList().get(0).getStatus();
		boolean nextStatus = DmpInputTaskStatusEnum.isNextStatus(status, updateTaskStatus);
		if(nextStatus) {
			dmpResponse.setDoOutputChain(false);
			dmpResponse.setDoChildCfgInput(false);
			dmpResponse.setDoUpdateStatus(false);
		}
		return nextStatus;
	}
	
	/**
	 * 获取子类任务
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
	protected List<DmpCfgInputChildEntity> getCfgInputChildList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse){
		return dmpCfgInputChildServiceImpl.lambdaQuery()
				.eq(DmpCfgInputChildEntity::getParentId, dmpResponse.getDmpCfgInputEntity().getId())
				.eq(DmpCfgInputChildEntity::getInputStatus, dmpRequest.getDealTaskStatus())
				.list();
	}
	
	protected void beforeToDoStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	protected void afterToDoStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	protected void beforeToDoOutputChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , List<String> outputClassList) {
		
	}
	
	protected void afterToDoOutputChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , Map<String, DmpOutputInitResponse> outputResultMap) {
		
	}
	
	protected void beforeToDoChildCfgInput(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , List<String> nextLevelIdList) {
		
	}
	
	protected void afterToDoChildCfgInput(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , List<DmpInputHotfixCreateRequest> dmpInputHotfixCreateRequestList) {
		
	}
	
	protected void beforeToDoNextChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	protected void afterToDoNextChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	protected void beforeToDoUpdateTaskStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	protected void afterToDoUpdateTaskStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , boolean updateSuccess) {
		 
	}
}
