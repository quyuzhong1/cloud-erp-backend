package com.erp.server.dmp.inout.handler.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
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
import com.erp.server.dmp.inout.handler.input.create.DmpInputHistoryCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputHotfixCreateHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputNormalCreateHandler;

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
	private DmpInputHotfixCreateHandler dmpInputHotfixCreateHandler;
	@Autowired
	private DmpInputChildCreateHandler dmpInputChildCreateHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	@Autowired
	private DmpInputTaskFactory dmpInputTaskFactory;
	@Autowired
	@Qualifier("dmpInputChildExecutorPool")
	private ExecutorService dmpInputChildExecutorPool;
	
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
		for(DmpInputTaskEntity dmpInputTaskEntity : afterDmpInputTaskEntityList) {
			DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
			dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
			dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
			dmpInputFinishResponseList.add(dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest));
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
		List<DmpInputFinishResponse> dmpInputFinishResponseList = new LinkedList<>();
        //线程安全
        List<DmpInputFinishResponse> sycList = Collections.synchronizedList(dmpInputFinishResponseList);
        
		DmpInputCreateResponse dmpInputCreateResponse = this.createChildInputTask(dmpInputChildCreateRequest);
		List<DmpInputTaskEntity> afterDmpInputTaskEntityList = dmpInputCreateResponse.getAfterDmpInputTaskEntityList();
//		CountDownLatch countDownLatch = new CountDownLatch(afterDmpInputTaskEntityList.size());
		for(DmpInputTaskEntity dmpInputTaskEntity : afterDmpInputTaskEntityList) {
			DmpInputFinishRequest dmpInputFinishRequest = new DmpInputFinishRequest();
			dmpInputFinishRequest.setInputTaskId(dmpInputTaskEntity.getId());
			dmpInputFinishRequest.setExecTimeout(dmpInputTaskEntity.getExecTimeout());
			sycList.add(dmpInputTaskFactory.dealInputTask(dmpInputFinishRequest));
//			countDownLatch.countDown();
		}
//		try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//        	log.error("多线程执行父任务{}下的子任务失败" , dmpInputChildCreateRequest.getParentInputTaskId(), e);
//        	throw new ServiceException("多线程执行父任务下的子任务失败");
//        }
		return sycList;
	}
}
