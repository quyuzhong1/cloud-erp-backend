package com.erp.server.dmp.inout.handler.input.task.fds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputFdsRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputFdsRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFdsResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;
import com.erp.server.dmp.service.DmpInputTaskFileService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入任务处理器，被各种fds任务状态执行器继承，protected方法全部都可重写，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
public abstract class DmpInputFdsHandler extends DmpInputTaskHandler{
	
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
		updateTaskStatus = DmpInputTaskStatusEnum.FDS;
		this.beforeToDoStatus(dmpRequest, dmpResponse);
		
		List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList = dmpInputTaskFileService.lambdaQuery()
					.eq(DmpInputTaskFileEntity::getMainId, inputTaskId)
					.eq(DmpInputTaskFileEntity::getFdsConvertId, convertId)
					.list();
		if(CollUtil.isNotEmpty(dmpInputTaskFileEntityList)) {
			this.isNextStatus(dmpResponse);
		}else {
			Map<DmpCfgInputConvertEntity, List<DmpInputTaskInitDTO>> convertInputTaskInitDTOListMaps = dmpResponse.getConvertInputTaskInitDTOListMaps();
			if(convertInputTaskInitDTOListMaps != null && convertInputTaskInitDTOListMaps.size() > 0) {
				dmpInputTaskFileEntityList = this.uploadInitToFds(dmpRequest,  dmpResponse);
			}else {
				dmpInputTaskFileEntityList = this.uploadNoneToFds(dmpRequest,  dmpResponse);
			}
		}

		dmpResponse.getConvertInputTaskFileEntityListMaps().put(dmpCfgInputConvertEntity, dmpInputTaskFileEntityList);
		this.afterToDoStatus(dmpRequest, dmpResponse);
		
		DmpOutputFdsRequest dmpOutputFdsRequest = new DmpOutputFdsRequest();
		dmpOutputFdsRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		dmpOutputFdsRequest.setConvertInputTaskFileEntityListMaps(dmpResponse.getConvertInputTaskFileEntityListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputFdsRequest);
	}
	
	public abstract List<DmpInputTaskFileEntity> uploadInitToFds(DmpInputFdsRequest dmpRequest, DmpInputInitResponse dmpResponse);
	public abstract List<DmpInputTaskFileEntity> uploadNoneToFds(DmpInputFdsRequest dmpRequest, DmpInputTaskResponse dmpResponse);
	
	@Override
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpInputFdsResponse dmpInputFdsResponse = (DmpInputFdsResponse) dmpResponse;
		List<DmpInputTaskFileEntity> list = dmpInputFdsResponse.getConvertInputTaskFileEntityListMaps().get(dmpCfgInputConvertEntity);
		if(CollUtil.isNotEmpty(list)) {
			return list.stream().map(DmpInputTaskFileEntity::getId).collect(Collectors.toList());
		}else {
			return super.getNextLevelIdList(dmpRequest, dmpResponse);
		}
	}
}
