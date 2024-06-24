package com.erp.server.dmp.inout.handler.input.task.finish;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputBaseDmpHandler;

@Service
public class DmpInputBaseFinishHandler extends DmpInputFinishHandler{

	@Autowired
	private DmpInputBaseDmpHandler dmpInputBaseDmpHandler;
	
	@Override
	public void dealDmpToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {

	}

	@Override
	public void dealMongoToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
		dmpInputBaseDmpHandler.convertMongoToDmp(dmpRequest, dmpResponse);
	}

	@Override
	public void dealFdsToFinish(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse) {
		dmpInputBaseDmpHandler.convertFdsToDmp(dmpRequest, dmpResponse);
	}

	@Override
	public void dealInitToFinish(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse) {
		dmpInputBaseDmpHandler.convertInitToDmp(dmpRequest, dmpResponse);
	}

	@Override
	public void dealNoneToFinish(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		
	}
	
	
	

}
