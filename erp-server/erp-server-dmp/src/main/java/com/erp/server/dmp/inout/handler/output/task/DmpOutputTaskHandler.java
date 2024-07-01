package com.erp.server.dmp.inout.handler.output.task;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpOutputTaskDto;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;
import com.erp.server.dmp.service.DmpOutputTaskService;

public abstract class DmpOutputTaskHandler extends DmpOutputHandler{
	@Autowired
	private DmpOutputTaskService dmpOutputTaskService;
	
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputTaskRequest)) {
            throw new ServiceException(dmpRequest + " not DmpOutputTaskRequest");
        }
        if (!(dmpResponse instanceof DmpOutputTaskResponse)) {
            throw new ServiceException(dmpResponse + " not DmpOutputTaskResponse");
        }
        doDmpHandler((DmpOutputTaskRequest) dmpRequest, (DmpOutputTaskResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse, DmpHandlerChain chain) {
		DmpOutputTaskDto outputData = this.outputData(dmpRequest, dmpResponse);
		dmpOutputTaskService.lambdaUpdate()
				.eq(DmpOutputTaskEntity::getId, dmpRequest.getOutputTaskId())
				.set(DmpOutputTaskEntity::getStatus, DmpOutputTaskStatusEnum.FINISH.getCode())
				.set(DmpOutputTaskEntity::getRequestData, outputData.getRequestData())
				.set(DmpOutputTaskEntity::getResponseData, outputData.getResponseData())
				.set(DmpOutputTaskEntity::getUpdateTime, LocalDateTime.now())
				.update();
		
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract DmpOutputTaskDto outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse);
}
