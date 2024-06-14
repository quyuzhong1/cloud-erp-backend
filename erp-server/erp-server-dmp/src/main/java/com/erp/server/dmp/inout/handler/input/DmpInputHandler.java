package com.erp.server.dmp.inout.handler.input;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;

/**
 * dmp输入任务处理器
 * @author Administrator
 *
 */
public abstract class DmpInputHandler implements DmpHandler{
	
	protected DmpCfgInputConvertEntity dmpCfgInputConvertEntity;
	
	public void setDmpCfgInputConvertEntity(DmpCfgInputConvertEntity dmpCfgInputConvertEntity) {
		this.dmpCfgInputConvertEntity = dmpCfgInputConvertEntity;
	}

	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputRequest)) {
            throw new ServiceException(dmpRequest + " not DmpInputRequest");
        }
        if (!(dmpResponse instanceof DmpInputResponse)) {
            throw new ServiceException(dmpResponse + " not DmpInputResponse");
        }
        doDmpHandler((DmpInputRequest) dmpRequest, (DmpInputResponse) dmpResponse, chain);
	}
	
	protected void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
}
