package com.erp.server.dmp.inout.handler.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputChildCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputChildCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputCompensateCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputHistoryCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputHotfixCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputNormalCreateHandler;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 输入任务创建工厂，添加handler给handler链路执行
 * @author Administrator
 *
 */
@Component
@Slf4j
public class DmpInputCreateFactory{
	
	@Autowired
	private DmpInputNormalCreateHandler dmpInputNormalCreateHandler;
	@Autowired
	private DmpInputHistoryCreateHandler dmpInputHistoryCreateHandler;
	@Autowired
	private DmpInputCompensateCreateHandler dmpInputCompensateCreateHandler;
	@Autowired
	private DmpInputHotfixCreateHandler dmpInputHotfixCreateHandler;
	@Autowired
	private DmpInputChildCreateHandler dmpInputChildCreateHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	
	/**
	 * 创建正常任务
	 * @param dmpInputCreateRequest
	 */
	public void createNormalInputTask(DmpInputCreateRequest dmpInputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputNormalCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		bean.doDmpHandler(dmpInputCreateRequest, new DmpInputCreateResponse());
	}
	
	/**
	 * 创建历史任务
	 * @param dmpInputCreateRequest
	 */
	public void createHistoryInputTask(DmpInputCreateRequest dmpInputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputHistoryCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		bean.doDmpHandler(dmpInputCreateRequest, new DmpInputCreateResponse());
	}
	
	/**
	 * 创建补偿任务
	 * @param dmpInputCreateRequest
	 */
	public void createCompensateInputTask(DmpInputCreateRequest dmpInputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputCompensateCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		bean.doDmpHandler(dmpInputCreateRequest, new DmpInputCreateResponse());
	}
	
	/**
	 * 创建快速任务
	 * @param dmpInputHotfixCreateRequest
	 * @return
	 */
	public DmpInputCreateResponse createHotfixInputTask(DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputHotfixCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		DmpInputCreateResponse dmpResponse = new DmpInputCreateResponse();
		bean.doDmpHandler(dmpInputHotfixCreateRequest, dmpResponse);
		return dmpResponse;
		
	}
	
	/**
	 * 创建子类任务
	 * @param dmpInputChildCreateRequest
	 * @return
	 */
	public DmpInputCreateResponse createChildInputTask(DmpInputChildCreateRequest dmpInputChildCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputChildCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		DmpInputCreateResponse dmpResponse = new DmpInputCreateResponse();
		bean.doDmpHandler(dmpInputChildCreateRequest, dmpResponse);
		return dmpResponse;
		
	}
	
	/**
	 * 创建快速任务并立马执行
	 * @param dmpInputHotfixCreateRequest
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DmpInputFinishResponse> doHotfixInputTask(DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest) {
		List<DmpInputFinishResponse> dmpInputFinishResponseList = new ArrayList<>();
		DmpInputCreateResponse dmpInputCreateResponse = this.createHotfixInputTask(dmpInputHotfixCreateRequest);
		List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputCreateResponse.getAfterDmpInputTaskEntityList();
		if(CollUtil.isNotEmpty(afterDmpInputTaskEntityList)) {
			for(DmpInputTaskEntity dmpInputTaskEntity : afterDmpInputTaskEntityList) {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
				dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
				dmpInputFinishResponseList.add(dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest));
			}
		}
		return dmpInputFinishResponseList;
	}
	
	/**
	 * 创建子类任务并立马执行
	 * @param dmpInputHotfixCreateRequest
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<DmpInputFinishResponse> doChildInputTask(DmpInputChildCreateRequest dmpInputChildCreateRequest) {
		List<DmpInputFinishResponse> dmpInputFinishResponseList = new ArrayList<>();
		DmpInputCreateResponse dmpInputCreateResponse = this.createChildInputTask(dmpInputChildCreateRequest);
		List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputCreateResponse.getAfterDmpInputTaskEntityList();
		if(CollUtil.isNotEmpty(afterDmpInputTaskEntityList)) {
			for(DmpInputTaskEntity dmpInputTaskEntity : afterDmpInputTaskEntityList) {
				DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
				dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
				dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
				dmpInputFinishResponseList.add(dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest));
			}
		}
		return dmpInputFinishResponseList;
	}
}
