package com.erp.server.dmp.inout.handler.input.task.fds;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFdsRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.DmpInputHandler;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputFdsHandler extends DmpInputHandler{
	
	@Autowired
	private DmpInputTaskFileService dmpInputTaskFileService;
	
	@Override
	public void doDmpHandler(DmpInputRequest dmpRequest, DmpInputResponse dmpResponse, DmpHandlerChain chain) {
		if (!(dmpRequest instanceof DmpInputFdsRequest)) {
			chain.doDmpHandler(dmpRequest, dmpResponse);
			return;
        }
        if (!(dmpResponse instanceof DmpInputFdsResponse)) {
        	chain.doDmpHandler(dmpRequest, dmpResponse);
        	return;
        }
        doDmpHandler((DmpInputFdsRequest) dmpRequest, (DmpInputFdsResponse) dmpResponse, chain);
	}
	
	private void doDmpHandler(DmpInputFdsRequest dmpRequest, DmpInputFdsResponse dmpResponse, DmpHandlerChain chain) {
		DmpInputTaskEntity dmpInputTaskEntity = dmpResponse.getBeforeDmpInputTaskEntityList().get(0);
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpInputTaskFileService.lambdaQuery().eq(DmpInputTaskFileEntity::getMainId, dmpInputTaskEntity.getId()).list();
		
		if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
			
		}else {
			List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = dmpResponse.getDmpInputTaskInitDTOList();
			if(CollUtil.isNotEmpty(dmpInputTaskInitDTOList)) {
				dmpInputTaskFileEntityList = uploadInitToFds(dmpRequest,  dmpResponse);
			}else {
				dmpInputTaskFileEntityList = uploadNoneToFds(dmpRequest,  dmpResponse);
			}
		}
		
		dmpResponse.setDmpInputTaskFileEntityList(dmpInputTaskFileEntityList);
		chain.doDmpHandler(dmpRequest, dmpResponse);
	}
	
	public abstract List<DmpInputTaskFileEntity> uploadInitToFds(DmpInputFdsRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<DmpInputTaskFileEntity> uploadNoneToFds(DmpInputFdsRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
