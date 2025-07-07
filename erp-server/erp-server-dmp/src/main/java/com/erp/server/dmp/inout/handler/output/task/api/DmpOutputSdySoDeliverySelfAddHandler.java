package com.erp.server.dmp.inout.handler.output.task.api;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdySoDeliverySelfAddHandler extends DmpOutputSdySoDeliveryHandler {
	@Override
	protected boolean isSelfAdd() {
		return true;
	}
}
