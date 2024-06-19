package com.erp.server.dmp.inout.handler.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.task.DmpInputBaseTaskHandler;
import com.erp.server.dmp.service.DmpInputTaskService;

@Component
public class DmpInputTaskFactory{
	
	@Autowired
	private DmpInputTaskService dmpInputTaskService;
	@Autowired
	private DmpInputBaseTaskHandler dmpInputBaseTaskHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	
	public DmpInputFinishResponse dealInputTask(DmpInputFinishRequest dmpInputFinishRequest) {
		DmpInputTaskStatusEnum[] values = DmpInputTaskStatusEnum.values();
		DmpInputFinishResponse dmpResponse = new DmpInputFinishResponse();
		for(DmpInputTaskStatusEnum value : values) {
			if(value != DmpInputTaskStatusEnum.INIT && value != DmpInputTaskStatusEnum.ERROR) {
				try {
					dmpInputFinishRequest.setDealTaskStatus(value);
					DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
					bean.addDmpHandler(dmpInputBaseTaskHandler);
					bean.addDmpHandler(dmpInputTaskStatusHandler);
					bean.doDmpHandler(dmpInputFinishRequest, dmpResponse);
				} catch (Exception e) {
					DmpCfgInputDetailEntity dmpCfgInputDetailEntity = dmpResponse.getDmpCfgInputDetailEntity();
					Integer maxRetryCount = dmpCfgInputDetailEntity.getMaxRetryCount();
					DmpInputTaskEntity dmpInputTaskEntity = dmpResponse.getBeforeDmpInputTaskEntityList().get(0);
					Integer errorCount = dmpInputTaskEntity.getErrorCount() + 1;
					boolean errorFlag = errorCount == maxRetryCount;
					dmpInputTaskService.updateErrorStatus(dmpInputTaskEntity.getId(), errorFlag, errorCount, e);
					throw e;
				}
			}
		}
		return dmpResponse;
	}
	
}
