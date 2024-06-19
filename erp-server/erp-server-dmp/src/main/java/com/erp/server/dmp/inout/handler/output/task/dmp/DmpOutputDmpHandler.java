package com.erp.server.dmp.inout.handler.output.task.dmp;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;

@Service
public abstract class DmpOutputDmpHandler extends DmpOutputHandler{
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputDmpRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpOutputDmpResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpOutputDmpRequest) dmpRequest, (DmpOutputDmpResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputDmpRequest dmpRequest, DmpOutputDmpResponse dmpResponse, DmpHandlerChain chain) {
		test(dmpRequest, dmpResponse);
		if(dmpRequest.isDoNextChain()) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract void test(DmpOutputDmpRequest dmpRequest, DmpOutputMongoResponse dmpResponse);
}
