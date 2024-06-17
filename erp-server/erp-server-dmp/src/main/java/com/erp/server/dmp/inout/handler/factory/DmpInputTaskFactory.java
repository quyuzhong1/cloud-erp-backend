package com.erp.server.dmp.inout.handler.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.task.DmpInputBaseTaskHandler;

@Component
public class DmpInputTaskFactory{
	
	@Autowired
	private DmpInputBaseTaskHandler dmpInputTaskHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	
	public void dealInputTask(DmpInputFinishRequest dmpInputFinishRequest) {
		DmpInputTaskStatusEnum[] values = DmpInputTaskStatusEnum.values();
		DmpInputFinishResponse dmpResponse = new DmpInputFinishResponse();
		for(DmpInputTaskStatusEnum value : values) {
			if(value != DmpInputTaskStatusEnum.INIT && value != DmpInputTaskStatusEnum.ERROR) {
				try {
					dmpInputFinishRequest.setDealTaskStatus(value);
					DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
					bean.addDmpHandler(dmpInputTaskHandler);
					bean.addDmpHandler(dmpInputTaskStatusHandler);
					bean.doDmpHandler(dmpInputFinishRequest, dmpResponse);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}
}
