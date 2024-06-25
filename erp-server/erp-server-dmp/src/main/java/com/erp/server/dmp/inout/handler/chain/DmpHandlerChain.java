package com.erp.server.dmp.inout.handler.chain;

import java.util.List;

import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;

/**
 * 执行handler链路
 * @author Administrator
 *
 */
public interface DmpHandlerChain {
	/**
	 * 执行handler
	 * @param dmpRequest
	 * @param dmpResponse
	 */
	void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse);
	/**
	 * 插入handler并放入第一个执行
	 * @param dmpHandlerList
	 */
	void addFirstDmpHandlerList(List<DmpHandler> dmpHandlerList);
}
