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
	
	/**
	 * 分布式事务下，本地事务不需要注册提交事务，等分布式事务最终提交
	 */
	private boolean needCommit = true;
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public void setNeedCommit(boolean needCommit) {
		this.needCommit = needCommit;
	}

	@Override
	public void afterCompletion(int status) {
		InventoryTransactionService bean = ApplicationContextUtils.getBean(InventoryTransactionService.class);
		if (status == STATUS_COMMITTED) {
			if(needCommit) {
				bean.commitRedis(transactionId , true);
			}
		}else {
			bean.rollbackRedis(transactionId);
		}
	}
	
	public static void register(String transactionId) {
		InventoryTransactionSynchronizationAdapter synchronization = new InventoryTransactionSynchronizationAdapter();
		synchronization.setTransactionId(transactionId);
		synchronization.setNeedCommit(!RootContext.inGlobalTransaction());
		if(TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}else {
			synchronization.afterCompletion(STATUS_COMMITTED);
		}
	}
	
	public void doXa(String transactionId , Integer status) {
		this.transactionId = transactionId;
		this.afterCompletion(status);
	}
	
}
