package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputMongoBaseEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputMongoRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputMongoHandler extends DmpInputHandler{
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputMongoRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputMongoResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputMongoRequest) dmpRequest, (DmpInputMongoResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputMongoRequest dmpRequest, DmpInputMongoResponse dmpResponse, DmpHandlerChain chain) {
		List<DmpInputMongoBaseEntity> dmpInputMongoBaseEntityList = null;
		
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpResponse.getDmpInputTaskFileEntityList();
		if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
			dmpInputMongoBaseEntityList = parseFdsToMongo(dmpRequest,  dmpResponse);
		}else {
			List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = dmpResponse.getDmpInputTaskInitDTOList();
			if(CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
				dmpInputMongoBaseEntityList = parseInitToMongo(dmpRequest, dmpResponse);
			}else {
				dmpInputMongoBaseEntityList = parseNoneToMongo(dmpRequest, dmpResponse);
			}
		}
		
		dmpResponse.setDmpInputMongoBaseEntityList(dmpInputMongoBaseEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<DmpInputMongoBaseEntity> parseFdsToMongo(DmpInputMongoRequest dmpRequest, DmpInputFdsResponse dmpResponse);
	public abstract List<DmpInputMongoBaseEntity> parseInitToMongo(DmpInputMongoRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<DmpInputMongoBaseEntity> parseNoneToMongo(DmpInputMongoRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
}
