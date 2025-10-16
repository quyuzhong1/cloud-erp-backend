package com.erp.server.wms.config;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.wms.service.InventoryTransactionService;

import io.seata.core.context.RootContext;

public class InventoryTransactionSynchronizationAdapter extends TransactionSynchronizationAdapter{
	/**
	 * 事务id
	 */
	private String transactionId;
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@Override
	public void afterCompletion(int status) {
		InventoryTransactionService bean = ApplicationContextUtils.getBean(InventoryTransactionService.class);
		if (status == STATUS_COMMITTED) {
			bean.commitRedis(transactionId);
		}else {
			bean.rollbackRedis(transactionId);
		}
	}
	
	public static void register(String transactionId) {
		if(RootContext.inGlobalTransaction()) {
			return;
		}
		InventoryTransactionSynchronizationAdapter synchronization = new InventoryTransactionSynchronizationAdapter();
		synchronization.setTransactionId(transactionId);
		TransactionSynchronizationManager.registerSynchronization(synchronization);
	}
	
	public void doXa(String transactionId , Integer status) {
		this.transactionId = transactionId;
		this.afterCompletion(status);
	}
	
}
