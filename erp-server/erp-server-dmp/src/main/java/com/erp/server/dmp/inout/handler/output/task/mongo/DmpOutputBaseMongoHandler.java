package com.erp.server.dmp.inout.handler.output.task.mongo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputMongoRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputFdsResponse;

@Service
@Scope("prototype")
public class DmpOutputBaseMongoHandler extends DmpOutputMongoHandler{

	@Override
	public void test(DmpOutputMongoRequest dmpRequest, DmpOutputFdsResponse dmpResponse) {
		
	}
	
}
