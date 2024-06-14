package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputInitHandler extends DmpInputHandler{
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
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
		List<DmpInputTaskEntity> befortDmpInputTaskEntityList = dmpResponse.getBeforeDmpInputTaskEntityList();
		if(CollUtil.isEmpty(befortDmpInputTaskEntityList)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
		}
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = getInitData(dmpRequest , dmpResponse);
		dmpResponse.setDmpInputTaskInitDTOList(dmpInputTaskInitDTOList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
