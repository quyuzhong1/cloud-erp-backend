package com.erp.server.dmp.inout.handler.etl;

import org.springframework.stereotype.Service;

import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.request.DmpEtlRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpEtlResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;

/**
 * dmpEtl任务处理器
 * @author Administrator
 *
 */
@Service
public abstract class DmpEtlHandler implements DmpHandler{
	
	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpEtlRequest)) {
            throw new ServiceException(dmpRequest + " not DmpEtlRequest");
        }
        if (!(dmpResponse instanceof DmpEtlResponse)) {
            throw new ServiceException(dmpResponse + " not DmpEtlResponse");
        }
        doDmpHandler((DmpEtlRequest) dmpRequest, (DmpEtlResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpEtlRequest dmpRequest, DmpEtlResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
}
