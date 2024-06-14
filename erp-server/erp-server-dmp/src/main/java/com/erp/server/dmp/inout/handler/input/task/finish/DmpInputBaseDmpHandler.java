package com.erp.server.dmp.inout.handler.input.task.finish;

import org.springframework.beans.factory.annotation.Autowired;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpInputTaskService;

public class DmpInputBaseDmpHandler extends DmpInputFinishHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;

	@Override
	public void dealDmpToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
		String inputTaskId = dmpRequest.getInputTaskId();
		dmpInputTaskService.lambdaUpdate()
			.eq(DmpInputTaskEntity::getId, inputTaskId)
			.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.FINISH.getCode())
			.update();
	}

	@Override
	public void dealMongoToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dealFdsToFinish(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dealInitToFinish(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dealNoneToFinish(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
