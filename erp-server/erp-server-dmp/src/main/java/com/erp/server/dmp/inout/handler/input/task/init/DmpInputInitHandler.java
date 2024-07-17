package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.List;

import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;

/**
 * dmp输入任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
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
		DmpOutputTaskRequest dmpOutputInitRequest = new DmpOutputTaskRequest();
		dmpOutputInitRequest.setConvertInputTaskInitDTOListMaps(dmpResponse.getConvertInputTaskInitDTOListMaps());
		this.doBaseChain(dmpRequest, dmpResponse, chain, dmpOutputInitRequest);
	}
	
	/**
	 * 获取外部初始数据
	 * @param dmpRequest
	 * @param dmpResponse
	 * @return
	 */
	public abstract List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse);
}
