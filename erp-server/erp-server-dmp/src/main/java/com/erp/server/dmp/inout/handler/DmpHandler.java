package com.erp.server.dmp.inout.handler;

import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.chain.DmpHandlerChain;

/**
 * dmp输入输出任务处理器
 * @author Administrator
 *
 */
public interface DmpHandler{
	
	void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse, DmpHandlerChain chain);
	
}
