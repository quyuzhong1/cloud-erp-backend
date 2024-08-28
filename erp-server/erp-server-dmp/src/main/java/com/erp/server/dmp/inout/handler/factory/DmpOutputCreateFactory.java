package com.erp.server.dmp.inout.handler.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.QueryParam;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputHotfixCreateHandler;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputInputCreateHandler;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 输出任务创建工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Component
@Slf4j
public class DmpOutputCreateFactory{
	@Autowired
	private DmpOutputInputCreateHandler dmpOutputInputCreateHandler;
	@Autowired
	private DmpOutputHotfixCreateHandler dmpOutputHotfixCreateHandler;
	@Autowired
	private DmpOutputTaskFactory dmpOutputTaskFactory;
	@Autowired
	private DmpHandlerCache dmpHandlerCache;
	
	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createInputOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputInputCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建输入任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doInputOutputTask(DmpOutputInputCreateRequest dmpRequest) {
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createInputOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = dmpRequest.getDmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}
	
	/**
	 * 创建输入任务类型输出任务
	 * @param dmpOutputCreateRequest
	 */
	public DmpOutputCreateResponse createHotfixOutputTask(DmpOutputCreateRequest dmpRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpOutputHotfixCreateHandler);
		DmpOutputCreateResponse dmpResponse = new DmpOutputCreateResponse();
		bean.doDmpHandler(dmpRequest, dmpResponse);
		return dmpResponse;
	}
	
	/**
	 * 创建快速任务类型立马执行
	 * @param dmpRequest
	 */
	@Transactional(rollbackFor = Exception.class)
	public DmpOutputTaskResponse doHotfixOutputTask(DmpOutputHotfixCreateRequest dmpRequest) {
		dmpRequest.setThrowException(true);
		DmpOutputCreateResponse dmpOutputCreateResponse = this.createHotfixOutputTask(dmpRequest);
		DmpOutputTaskRequest dmpOutputTaskRequest = new DmpOutputTaskRequest();
		DmpOutputTaskEntity dmpOutputTaskEntity = dmpOutputCreateResponse.getAfterDmpOutputTaskEntityList().get(0);
		dmpOutputTaskRequest.setNotValidate(true);
		dmpOutputTaskRequest.setOutputTaskId(dmpOutputTaskEntity.getId());
		dmpOutputTaskRequest.setExecTimeout(dmpOutputTaskEntity.getExecTimeout());
		
		DmpCfgOutputEntity dmpCfgOutputEntity = dmpOutputCreateResponse.getDmpCfgOutputEntity();
		String inputConvertId = dmpCfgOutputEntity.getInputConvertId();
		DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> d.getId().equals(inputConvertId)).get(0);
		String inputStatus = dmpCfgInputConvertEntity.getInputStatus();
		if(DmpInputTaskStatusEnum.FDS.getCode().equals(inputStatus)) {
			throw new ServiceException("推送fds数据未实现");
		}else if(DmpInputTaskStatusEnum.MONGO.getCode().equals(inputStatus)) {
			throw new ServiceException("推送mongo数据未实现");
		}else if(DmpInputTaskStatusEnum.DMP.getCode().equals(inputStatus)) {
			List<QueryParam> queryParams = dmpRequest.getQueryParams();
			QueryWrapper<?> queryWrapper = QueryParam.getQueryWrapper(queryParams);
			List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList = dmpHandlerCache
					.getDmpCfgInputConvertEntityList(d -> d.getMainId().equals(dmpCfgInputConvertEntity.getMainId()) && d.getInputStatus().equals(inputStatus));
			dmpCfgInputConvertEntityList.sort((d1 , d2) -> d1.getOrder().compareTo(d2.getOrder()));
			
			DmpCfgInputConvertEntity mainDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(mainDmpCfgInputConvertEntity.getStorageName(), true) + "ServiceImpl" , ServiceImpl.class);
			List<BaseEntity> list = serviceImpl.list(queryWrapper);
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(mainDmpCfgInputConvertEntity, list);
			if(CollUtil.isNotEmpty(list)) {
				String outputClass = dmpCfgOutputEntity.getOutputClass();
				DmpOutputTaskHandler dmpOutputTaskHandler = ApplicationContextUtils.getBean(DmpHandlerUtils.dealBeanClass(outputClass) , DmpOutputTaskHandler.class);
				dmpOutputTaskHandler.getRetryPushSourceData(dmpCfgInputConvertEntityList, dmpOutputTaskRequest);
			}
		}
		
		DmpOutputTaskResponse dmpOutputTaskResponse = dmpOutputTaskFactory.dealOutputTask(dmpOutputTaskRequest);
		return dmpOutputTaskResponse;
	}
}
