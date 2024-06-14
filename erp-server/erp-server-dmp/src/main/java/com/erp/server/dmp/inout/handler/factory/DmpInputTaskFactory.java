package com.erp.server.dmp.inout.handler.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.dmp.inout.dto.request.DmpInputFinishRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.task.DmpInputTaskHandler;

@Component
public class DmpInputTaskFactory{
	
	@Autowired
	private DmpInputTaskHandler dmpInputTaskHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	
	public void dealInputTask(DmpInputFinishRequest dmpInputFinishRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputTaskHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		bean.doDmpHandler(dmpInputFinishRequest, new DmpInputFinishResponse());
	}
}
