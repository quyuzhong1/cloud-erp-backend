package com.erp.server.dmp.inout.handler.output.task.fds;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputFdsRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;

@Service
@Scope("prototype")
public class DmpOutputBaseFdsHandler extends DmpOutputFdsHandler{

	@Override
	public void test(DmpOutputFdsRequest dmpRequest, DmpOutputInitResponse dmpResponse) {
		
	}

}
