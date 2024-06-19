package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputInitResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.output.task.init.DmpOutputInitHandler;

import cn.hutool.core.collection.CollUtil;

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
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = getInitData(dmpRequest , dmpResponse);
		dmpResponse.getConvertInputTaskInitDTOListMaps().put(dmpCfgInputConvertEntity, dmpInputTaskInitDTOList);
		
		if(dmpResponse.isDoOutputChain()) {
			List<String> outputClassList = this.getOutputClassList(dmpRequest, dmpResponse);
			if(CollUtil.isNotEmpty(outputClassList)) {
				for(String outputClass : outputClassList) {
					DmpOutputInitHandler dmpHandlerBean = this.getDmpHandlerBean(outputClass, DmpOutputInitHandler.class);
					DmpOutputInitRequest dmpOutputInitRequest = new DmpOutputInitRequest();
					dmpOutputInitRequest.setDoNextChain(false);
					dmpOutputInitRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
					dmpHandlerBean.doDmpHandler(dmpOutputInitRequest, new DmpOutputInitResponse(), chain);
				}
			}
		}
		
		if(dmpResponse.isDoNextChain()) {
			dmpResponse.setDoOutputChain(true);
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
