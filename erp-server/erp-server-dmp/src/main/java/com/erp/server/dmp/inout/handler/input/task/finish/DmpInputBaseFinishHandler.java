package com.erp.server.dmp.inout.handler.input.task.finish;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;

@Service
public class DmpInputBaseFinishHandler extends DmpInputFinishHandler{

	@Override
	public void dealDmpToFinish(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {

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
