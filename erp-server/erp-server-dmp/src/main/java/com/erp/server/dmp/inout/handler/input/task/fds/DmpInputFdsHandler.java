package com.erp.server.dmp.inout.handler.input.task.fds;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFdsRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputFdsRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpOutputFdsResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.inout.handler.output.task.fds.DmpOutputFdsHandler;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;

@Service
public abstract class DmpInputFdsHandler extends DmpInputTaskHandler{
	
	protected String inputTaskId;
	protected String convertId;
	
	@Autowired
	private DmpInputTaskFileService dmpInputTaskFileService;
	
	@Override
	public void doDmpHandler(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse, DmpHandlerChain chain) {
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
		inputTaskId = dmpRequest.getInputTaskId();
		convertId = dmpCfgInputConvertEntity.getId();
		
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpInputTaskFileService.lambdaQuery()
					.eq(DmpInputTaskFileEntity::getMainId, inputTaskId)
					.eq(DmpInputTaskFileEntity::getFdsConvertId, dmpCfgInputConvertEntity.getId())
					.list();
		
		if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
			String status = dmpResponse.getBeforeDmpInputTaskEntityList().get(0).getStatus();
			if(DmpInputTaskStatusEnum.FDS.getCode().equals(status) || DmpInputTaskStatusEnum.MONGO.getCode().equals(status) || DmpInputTaskStatusEnum.DMP.getCode().equals(status) || DmpInputTaskStatusEnum.FINISH.getCode().equals(status)) {
				dmpResponse.setDoUpdateStatus(false);
				dmpResponse.setDoOutputChain(false);
			}
		}else {
			Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
			if(convertInputTaskInitDTOListMaps != null && convertInputTaskInitDTOListMaps.size() > 0) {
				dmpInputTaskFileEntityList = uploadInitToFds(dmpRequest,  dmpResponse);
			}else {
				dmpInputTaskFileEntityList = uploadNoneToFds(dmpRequest,  dmpResponse);
			}
		}

		dmpResponse.getConvertInputTaskFileEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputTaskFileEntityList);
	
		if(dmpResponse.isDoOutputChain()) {
			List<String> outputClassList = this.getOutputClassList(dmpRequest, dmpResponse);
			if(CollUtil.isNotEmpty(outputClassList)) {
				for(String outputClass : outputClassList) {
					DmpOutputFdsHandler dmpHandlerBean = this.getDmpHandlerBean(outputClass, DmpOutputFdsHandler.class);
					DmpOutputFdsRequest dmpOutputFdsRequest = new DmpOutputFdsRequest();
					dmpOutputFdsRequest.setDoNextChain(false);
					dmpOutputFdsRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
					dmpOutputFdsRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
					dmpHandlerBean.doDmpHandler(dmpOutputFdsRequest, new DmpOutputFdsResponse(), chain);
				}
			}
		}
		
		if(dmpResponse.isDoUpdateStatus()) {
			this.updateTaskStatus(DmpInputTaskStatusEnum.FDS);
		}
		
		if(dmpResponse.isDoNextChain()) {
			dmpResponse.setDoUpdateStatus(true);
			dmpResponse.setDoOutputChain(true);
			chain.doDmpHandler(dmpRequest, dmpResponse);
		}
	}
	
	public abstract List<DmpInputTaskFileEntity> uploadInitToFds(DmpInputFdsRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<DmpInputTaskFileEntity> uploadNoneToFds(DmpInputFdsRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
