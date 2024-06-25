package com.erp.server.dmp.inout.handler.output.task.fds;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.request.DmpOutputFdsRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.output.DmpOutputHandler;

@Service
public abstract class DmpOutputFdsHandler extends DmpOutputHandler{
	@Override
	public void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputFdsRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpOutputFdsResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpOutputFdsRequest) dmpRequest, (DmpOutputFdsResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpOutputFdsRequest dmpRequest, DmpOutputFdsResponse dmpResponse, DmpHandlerChain chain) {
		test(dmpRequest, dmpResponse);
		if(dmpRequest.isDoNextChain()) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract void test(DmpOutputFdsRequest dmpRequest, DmpOutputInitResponse dmpResponse);
}
