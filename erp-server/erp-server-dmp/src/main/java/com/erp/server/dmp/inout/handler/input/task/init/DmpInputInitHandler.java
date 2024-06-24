package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;

@Service
public abstract class DmpInputInitHandler extends DmpInputTaskHandler{
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputInitRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputInitResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputInitRequest) dmpRequest, (DmpInputInitResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputInitRequest dmpRequest, DmpInputInitResponse dmpResponse, DmpHandlerChain chain) {
		this.beforeToDoStatus(dmpRequest, dmpResponse);
		
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = this.getInitData(dmpRequest , dmpResponse);
		
		dmpResponse.getConvertInputTaskInitDTOListMaps().put(dmpCfgInputConvertEntity, dmpInputTaskInitDTOList);
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		dmpResponse.setDoUpdateStatus(false);
		DmpOutputInitRequest dmpOutputInitRequest = new DmpOutputInitRequest();
		dmpOutputInitRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputInitRequest);
	}
	
	public abstract List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
