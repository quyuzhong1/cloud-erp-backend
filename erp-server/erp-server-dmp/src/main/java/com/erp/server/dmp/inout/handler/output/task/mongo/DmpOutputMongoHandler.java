package com.erp.server.dmp.inout.handler.output.task.mongo;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputMongoRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;

@Service
public abstract class DmpOutputMongoHandler extends DmpOutputHandler{
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputMongoRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpOutputMongoResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpOutputMongoRequest) dmpRequest, (DmpOutputMongoResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputMongoRequest dmpRequest, DmpOutputMongoResponse dmpResponse, DmpHandlerChain chain) {
		test(dmpRequest, dmpResponse);
		if(dmpRequest.isDoNextChain()) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract void test(DmpOutputMongoRequest dmpRequest, DmpOutputFdsResponse dmpResponse);
}
