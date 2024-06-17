package com.erp.server.dmp.inout.handler.output;

import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;

/**
 * dmp输出任务处理器
 * @author Administrator
 *
 */
public abstract class DmpOutputHandler implements DmpHandler{
	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpOutputRequest)) {
            throw new ServiceException(dmpRequest + " not DmpOutputRequest");
        }
        if (!(dmpResponse instanceof DmpOutputResponse)) {
            throw new ServiceException(dmpResponse + " not DmpOutputResponse");
        }
        doDmpHandler((DmpInputRequest) dmpRequest, (DmpInputResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpOutputRequest dmpRequest, DmpOutputResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
}
