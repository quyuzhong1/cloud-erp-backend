package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputDmpBaseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputMongoBaseEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpInputTaskService;

public class DmpInputBaseDmpHandler extends DmpInputDmpHandler{

	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	
	@Override
	public List<DmpInputDmpBaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputMongoResponse dmpResponse) {
		String inputTaskId = dmpRequest.getInputTaskId();
		List<DmpInputDmpBaseEntity> dmpInputDmpBaseEntityList = new ArrayList<>();
		DmpInputDmpBaseEntity dmpInputDmpBaseEntity = null;
		
		List<DmpInputMongoBaseEntity> dmpInputMongoBaseEntityList = dmpResponse.getDmpInputMongoBaseEntityList();
		for(DmpInputMongoBaseEntity dmpInputMongoBaseEntity : dmpInputMongoBaseEntityList) {
			dmpInputDmpBaseEntity = new DmpInputDmpBaseEntity();
			
			dmpInputDmpBaseEntity.setInputTaskId(dmpInputMongoBaseEntity.getInputTaskId());
			dmpInputDmpBaseEntity.setFileId(dmpInputMongoBaseEntity.getFileId());
			
			dmpInputDmpBaseEntityList.add(dmpInputDmpBaseEntity);
		}
		
		dmpInputTaskService.lambdaUpdate()
				.eq(DmpInputTaskEntity::getId, inputTaskId)
				.set(DmpInputTaskEntity::getStatus, DmpInputTaskStatusEnum.DMP.getCode())
				.update();
		
		return dmpInputDmpBaseEntityList;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertInitToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputInitResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DmpInputDmpBaseEntity> convertNoneToDmp(DmpInputDmpRequest dmpRequest,
			DmpInputTaskResponse dmpResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
