package com.erp.server.dmp.inout.handler.output.task.init;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;

@Service
public abstract class DmpOutputInitHandler extends DmpOutputHandler{
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputInitRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpOutputInitResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpOutputInitRequest) dmpRequest, (DmpOutputInitResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputInitRequest dmpRequest, DmpOutputInitResponse dmpResponse, DmpHandlerChain chain) {
		test(dmpRequest, dmpResponse);
//		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract void test(DmpOutputInitRequest dmpRequest, DmpOutputInitResponse dmpResponse);
}
