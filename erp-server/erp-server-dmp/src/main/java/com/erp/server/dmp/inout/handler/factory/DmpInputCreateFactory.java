package com.erp.server.dmp.inout.handler.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.dmp.inout.dto.request.DmpInputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChainImpl;
import com.erp.server.dmp.inout.handler.input.all.DmpInputTaskStatusHandler;
import com.erp.server.dmp.inout.handler.input.create.DmpInputNormalCreateHandler;

@Component
public class DmpInputCreateFactory{
	
	@Autowired
	private DmpInputNormalCreateHandler dmpInputNormalCreateHandler;
	@Autowired
	private DmpInputTaskStatusHandler dmpInputTaskStatusHandler;
	
	public void createNormalInputTask(DmpInputCreateRequest dmpInputCreateRequest) {
		DmpHandlerChainImpl bean = ApplicationContextUtils.getBean(DmpHandlerChainImpl.class);
		bean.addDmpHandler(dmpInputNormalCreateHandler);
		bean.addDmpHandler(dmpInputTaskStatusHandler);
		bean.doDmpHandler(dmpInputCreateRequest, new DmpInputCreateResponse());
	}
	
}
