package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputDmpBaseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputMongoBaseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputDmpResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputDmpHandler extends DmpInputHandler{
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputDmpRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputDmpResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputDmpRequest) dmpRequest, (DmpInputDmpResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputDmpRequest dmpRequest, DmpInputDmpResponse dmpResponse, DmpHandlerChain chain) {
		List<DmpInputDmpBaseEntity> dmpInputDmpBaseEntityList = null;
		
		List<DmpInputMongoBaseEntity> dmpInputMongoBaseEntityList = dmpResponse.getDmpInputMongoBaseEntityList();
		if(CollUtil.isNotEmpty(dmpInputMongoBaseEntityList)) {
			dmpInputDmpBaseEntityList = convertMongoToDmp(dmpRequest, dmpResponse);
		}else {
			List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpResponse.getDmpInputTaskFileEntityList();
			if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
				dmpInputDmpBaseEntityList = convertFdsToDmp(dmpRequest, dmpResponse);
			}else {
				List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = dmpResponse.getDmpInputTaskInitDTOList();
				if(CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
					dmpInputDmpBaseEntityList = convertInitToDmp(dmpRequest, dmpResponse);
				}else {
					dmpInputDmpBaseEntityList = convertNoneToDmp(dmpRequest, dmpResponse);
				}
			}
		}
		
		dmpResponse.setDmpInputDmpBaseEntityList(dmpInputDmpBaseEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<DmpInputDmpBaseEntity> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse);
	public abstract List<DmpInputDmpBaseEntity> convertFdsToDmp(DmpInputDmpRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<DmpInputDmpBaseEntity> convertInitToDmp(DmpInputDmpRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<DmpInputDmpBaseEntity> convertNoneToDmp(DmpInputDmpRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
