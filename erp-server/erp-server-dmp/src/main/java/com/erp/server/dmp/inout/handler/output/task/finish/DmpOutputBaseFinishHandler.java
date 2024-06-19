package com.erp.server.dmp.inout.handler.output.task.finish;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputDmpResponse;

@Service
@Scope("prototype")
public class DmpOutputBaseFinishHandler extends DmpOutputFinishHandler{

	@Override
	public void test(DmpOutputFinishRequest dmpRequest, DmpOutputDmpResponse dmpResponse) {
		
	}

}
