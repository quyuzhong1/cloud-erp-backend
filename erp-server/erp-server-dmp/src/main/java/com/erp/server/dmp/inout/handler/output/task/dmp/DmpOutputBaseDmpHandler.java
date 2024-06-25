package com.erp.server.dmp.inout.handler.output.task.dmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputMongoResponse;

@Service
@Scope("prototype")
public class DmpOutputBaseDmpHandler extends DmpOutputDmpHandler{

	@Override
	public void test(DmpOutputDmpRequest dmpRequest, DmpOutputMongoResponse dmpResponse) {
		
	}

}
