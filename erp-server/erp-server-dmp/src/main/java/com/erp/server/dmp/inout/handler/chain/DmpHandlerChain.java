package com.erp.server.dmp.inout.handler.chain;

import java.util.List;

import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;

public interface DmpHandlerChain {
	void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse);
	void addFirstDmpHandlerList(List<DmpHandler> dmpHandlerList);
}
