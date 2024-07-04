package com.erp.server.dmp.inout.handler.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInputCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.output.create.DmpOutputInputCreateHandler;

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
	private DmpOutputTaskFactory dmpOutputTaskFactory;
	
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
}
