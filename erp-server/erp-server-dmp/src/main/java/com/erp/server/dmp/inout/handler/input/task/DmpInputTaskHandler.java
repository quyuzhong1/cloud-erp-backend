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
import com.erp.model.dmp.entity.DmpCfgInputChildEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpCfgInputChildTransactionalTypeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputChildCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDmpHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgInputChildService;
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
 * dmp输入任务处理器，被各种任务状态执行器继承，protected方法全部都可重写，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
public abstract class DmpInputTaskHandler extends DmpInputHandler{
	@Autowired
	protected DmpHandlerCache dmpHandlerCache;
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
	protected DmpInputTaskFileService dmpInputTaskFileService;
	@Autowired
	protected MongoService mongoService;
	@Autowired
	protected DmpCfgInputService dmpCfgInputService;
	@Autowired
	protected DmpCfgInputChildService dmpCfgInputChildService;
	@Autowired
	protected DmpOutputCreateFactory dmpOutputCreateFactory;
	
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

	/**
	 *执行链执行方法，初始化公共参数
	 */
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
	
	/**
	 * 被子类重写
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param chain
	 */
	protected void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	/**
	 * 获取DmpHandler的springbean
	 * @param <T>
	 * @param beanClass
	 * @param clazz
	 * @return
	 */
	protected <T extends DmpHandler> T getDmpHandlerBean(String beanClass , Class<T> clazz) {
		if(StringUtils.isBlank(beanClass)) {
			return null;
		}
		return ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(beanClass) , clazz);
	}
	
	
	/**
	 * 根据输入任务配置获取各状态执行handler
	 * @param cfgInputId
	 * @param dmpInputTaskStatusEnum
	 * @return
	 */
	protected List<DmpCfgInputConvertEntity> getDmpCfgInputConvertEntityListByStatus(String cfgInputId , DmpInputTaskStatusEnum dmpInputTaskStatusEnum){
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(cfgInputId) 
				&& d.getInputStatus().equals(dmpInputTaskStatusEnum.getCode()) && Boolean.FALSE.equals(d.getDisabled()));
		dmpCfgInputConvertEntityList.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
		return dmpCfgInputConvertEntityList;
	}
	
	/**
	 *  当前输入状态下的输出handler类名集合
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
	protected List<DmpOutputInputCreateRequest> getDmpCfgOutputDetailEntity(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		List<DmpOutputInputCreateRequest> dmpOutputInputCreateRequestList = new ArrayList<>();

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
					DmpOutputInputCreateRequest dmpOutputInputCreateRequest = new DmpOutputInputCreateRequest();
					dmpOutputInputCreateRequest.setCfgOutputId(dmpCfgOutputEntity.getId());
					dmpOutputInputCreateRequest.setDmpCfgOutputDetailEntity(dmpCfgOutputDetailEntity);
					dmpOutputInputCreateRequest.setInputTaskId(inputTaskId);
					dmpOutputInputCreateRequestList.add(dmpOutputInputCreateRequest);
				}
			}
		}
	
		return dmpOutputInputCreateRequestList;
	}
	
	/**
	 * 更新输入任务状态并执行更新前后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
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
	
	/**
	 * 执行基础链路方法，目前有执行输出handler，执行子类任务生成及处理，更新任务状态，执行下一个handler
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param chain
	 * @param dmpOutputRequest
	 */
	protected void doBaseChain(DmpInputTaskRequest dmpRequest, DmpInputInitResponse dmpResponse , DmpHandlerChain chain , DmpOutputTaskRequest dmpOutputRequest) {
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
	 * 转换key方法，mongo和dmp状态handler重新方法
	 * @param originalKey
	 * @return
	 */
	protected List<String> convertKey(String originalKey) {
		return Collections.singletonList(originalKey);
	}
	
	/**
	 * 获取子任务的下一层级id集合
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse){
		return null;
	}
	
	/**
	 * 获取父类存储名称
	 * @param taskStatusEnum
	 * @return
	 */
	protected String getParentStorageName(DmpInputTaskStatusEnum taskStatusEnum) {
		String cfgInputId = dmpCfgInputEntity.getId();
		String parentStorageName = "";
		List<DmpCfgInputChildEntity> dmpCfgInputChildEntityList = dmpCfgInputChildService.lambdaQuery()
				.eq(DmpCfgInputChildEntity::getChildId, cfgInputId)
				.eq(DmpCfgInputChildEntity::getInputStatus, taskStatusEnum.getCode())
				.list();
		if(CollUtil.isNotEmpty(dmpCfgInputChildEntityList)) {
			String parentId = dmpCfgInputChildEntityList.get(0).getParentId();
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(parentId) 
					&& d.getInputStatus().equals(taskStatusEnum.getCode()));
			if(DmpInputTaskStatusEnum.MONGO == taskStatusEnum) {
				parentStorageName = DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputService.getById(parentId), dmpCfgInputConvertEntityList.get(0));
			}else if(DmpInputTaskStatusEnum.DMP == taskStatusEnum) {
				parentStorageName = dmpCfgInputConvertEntityList.get(0).getStorageName();
			}
		}
		return parentStorageName;
	}
	
	/**
	 * 处理转换文件数据，即fds层输出数据
	 * @param dmpResponse
	 */
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
			dmpResponse.setConvertInputTaskFileEntityListMaps(convertInputTaskFileEntityListMaps);
		}
	}
	
	/**
	 * 处理转换mongo数据，即mongo层输出数据
	 * @param dmpResponse
	 */
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
			dmpResponse.setConvertInputMongoEntityListMaps(convertInputMongoEntityListMaps);
		}
	}
	
	/**
	 * 处理转换dmp数据，即dmp层输出数据
	 * @param dmpResponse
	 */
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
			dmpResponse.setConvertInputDmpBaseEntityListMaps(convertInputDmpBaseEntityListMaps);
		}
	}
	
	/**
	 * 获取当前dmp层操作数据的ServiceImpl
	 * @param storageName
	 * @return
	 */
	protected ServiceImpl getServiceImpl(String storageName) {
		return ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
	}
	
	/**
	 * 处理子类任务
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param resultDmpInputMongoEntityList
	 */
	protected Map<String, List<DmpInputFinishResponse>> doChildCfgInput(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		Map<String, List<DmpInputFinishResponse>> cfgInputChildFinishResponseMap = new HashMap<>();
		List<String> nextLevelIdList = this.getNextLevelIdList(dmpRequest, dmpResponse);
		this.beforeToDoChildCfgInput(dmpRequest, dmpResponse , nextLevelIdList);
		if(CollUtil.isNotEmpty(nextLevelIdList)) {
			List<DmpCfgInputChildEntity> cfgInputChildList = this.getCfgInputChildList(dmpRequest , dmpResponse);
			if(CollUtil.isNotEmpty(cfgInputChildList)) {
				for(DmpCfgInputChildEntity cfgInputChild : cfgInputChildList) {
					String childCfgInputId = cfgInputChild.getChildId();
					DmpInputChildCreateRequest dmpInputHotfixCreateRequest = new DmpInputChildCreateRequest();
			    	dmpInputHotfixCreateRequest.setCfgInputId(childCfgInputId);
			    	dmpInputHotfixCreateRequest.setNextLevelIdList(nextLevelIdList);
			    	dmpInputHotfixCreateRequest.setParentInputTaskId(inputTaskId);
			    	if(DmpCfgInputChildTransactionalTypeEnum.SINGLE.getCode().equals(cfgInputChild.getTransactionalType())) {
			    		cfgInputChildFinishResponseMap.put(childCfgInputId, dmpInputCreateFactory.doChildInputTaskSingle(dmpInputHotfixCreateRequest));
			    	}else {
			    		cfgInputChildFinishResponseMap.put(childCfgInputId, dmpInputCreateFactory.doChildInputTaskGlobal(dmpInputHotfixCreateRequest));
			    	}
				}
			}
		}
		this.afterToDoChildCfgInput(dmpRequest, dmpResponse, cfgInputChildFinishResponseMap);
		return cfgInputChildFinishResponseMap;
	}
	
	/**
	 * 执行输出链路
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param chain
	 * @param dmpOutputRequest
	 */
	protected void doOutputChain(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse , DmpHandlerChain chain , DmpOutputTaskRequest dmpOutputRequest) {
		List<DmpOutputInputCreateRequest> dmpOutputInputCreateRequestList = this.getDmpCfgOutputDetailEntity(dmpRequest, dmpResponse);
		this.beforeToDoOutputChain(dmpRequest, dmpResponse , dmpOutputInputCreateRequestList);
		Map<DmpOutputInputCreateRequest, DmpOutputTaskResponse> outputResultMap = new HashMap<>();
		if(CollUtil.isNotEmpty(dmpOutputInputCreateRequestList)) {
			for(DmpOutputInputCreateRequest dmpOutputInputCreateRequest : dmpOutputInputCreateRequestList) {
				dmpOutputInputCreateRequest.setDmpOutputTaskRequest(BeanUtil.copyProperties(dmpOutputRequest, DmpOutputTaskRequest.class));
				outputResultMap.put(dmpOutputInputCreateRequest, dmpOutputCreateFactory.doInputOutputTask(dmpOutputInputCreateRequest));
			}
		}
		this.afterToDoOutputChain(dmpRequest, dmpResponse , outputResultMap);
	}
	
	/**
	 * 执行下一个链路handler
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param chain
	 */
	protected void doNextChain(DmpInputTaskRequest dmpRequest, DmpInputInitResponse dmpResponse , DmpHandlerChain chain) {
		dmpResponse.setDoOutputChain(true);
		dmpResponse.setDoChildCfgInput(true);
		dmpResponse.setDoUpdateStatus(true);
		this.beforeToDoNextChain(dmpRequest, dmpResponse);
		chain.doDmpHandler(dmpRequest, dmpResponse);
		this.afterToDoNextChain(dmpRequest, dmpResponse);
	}
	
	/**
	 * 判断是否已经到下一个状态
	 * @param dmpResponse
	 * @return
	 */
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
	
	/**-----------------------------------------------------------下方都为各状态执行前后预置方法，全部用来继承-----------------------------------------------------------*/
	/**
	 * 处理当前状态前方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void beforeToDoStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	/**
	 * 处理当前状态后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void afterToDoStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	/**
	 * 执行输出链接前方法
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param outputClassList
	 */
	protected void beforeToDoOutputChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , List<DmpOutputInputCreateRequest> dmpOutputInputCreateRequestList) {
		
	}
	
	/**
	 * 执行输出链接后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param outputClassList
	 */
	protected void afterToDoOutputChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , Map<DmpOutputInputCreateRequest, DmpOutputTaskResponse> outputResultMap) {
		
	}
	
	/**
	 * 执行子类任务前方法
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param nextLevelIdList
	 */
	protected void beforeToDoChildCfgInput(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , List<String> nextLevelIdList) {
		
	}
	
	/**
	 * 执行子类任务后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 * @param nextLevelIdList
	 */
	protected void afterToDoChildCfgInput(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , Map<String, List<DmpInputFinishResponse>> cfgInputChildFinishResponseMap) {
		
	}
	
	/**
	 * 更新任务状态前方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void beforeToDoUpdateTaskStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	/**
	 * 更新任务状态后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void afterToDoUpdateTaskStatus(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse , boolean updateSuccess) {
		 
	}
	
	/**
	 * 执行下一链路前方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void beforeToDoNextChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
	/**
	 * 执行下一链路后方法
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	protected void afterToDoNextChain(DmpInputTaskRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		 
	}
	
}
