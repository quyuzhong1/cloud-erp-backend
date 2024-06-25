package com.erp.server.dmp.inout.handler.output.task.finish;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputFinishRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;

@Service
public abstract class DmpOutputFinishHandler extends DmpOutputHandler{
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputFinishRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpOutputFinishResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpOutputFinishRequest) dmpRequest, (DmpOutputFinishResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputFinishRequest dmpRequest, DmpOutputFinishResponse dmpResponse, DmpHandlerChain chain) {
		test(dmpRequest, dmpResponse);
		if(dmpRequest.isDoNextChain()) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract void test(DmpOutputFinishRequest dmpRequest, DmpOutputDmpResponse dmpResponse);
}
