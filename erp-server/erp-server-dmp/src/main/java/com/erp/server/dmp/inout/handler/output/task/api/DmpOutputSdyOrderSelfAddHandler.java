package com.erp.server.dmp.inout.handler.output.task.api;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


/**
 * 数帝云线下订单映射推送
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyOrderSelfAddHandler extends DmpOutputSdyOrderHandler {
	@Override
	protected boolean isSelfAdd() {
		return true;
	}
}
