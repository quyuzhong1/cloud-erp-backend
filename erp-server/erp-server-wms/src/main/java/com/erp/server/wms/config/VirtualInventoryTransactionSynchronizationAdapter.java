package com.erp.server.wms.config;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.seata.core.context.RootContext;

public class VirtualInventoryTransactionSynchronizationAdapter extends TransactionSynchronizationAdapter{
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
		InventoryRedisTxSynchronizationHelper.afterVirtualCompletion(transactionId, status, needCommit);
	}

	@Override
	public int getOrder() {
		// afterCompletion 升序执行：order 更小者先跑 Redis commit/rollback
		return InventoryTxSynchronizationOrder.REDIS_TX_CALLBACK;
	}
	
	public static void register(String transactionId) {
		VirtualInventoryTransactionSynchronizationAdapter synchronization = new VirtualInventoryTransactionSynchronizationAdapter();
		synchronization.setTransactionId(transactionId);
		synchronization.setNeedCommit(!RootContext.inGlobalTransaction());
		if(TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}else {
			synchronization.afterCompletion(STATUS_COMMITTED);
		}
	}
	
	/**
	 * Seata XA 回调：虚拟库存 Redis 提交/回滚完成后释放未分配共享锁。
	 * {@link io.seata.rm.datasource.xa.InventoryXAUtil} 先调实体 adapter 再调本方法，锁须在此处统一释放。
	 */
	public void doXa(String transactionId , Integer status) {
		this.transactionId = transactionId;
		this.afterCompletion(status);
		PgUnallocLockDeferredRegistry.releaseByTransactionId(transactionId);
	}
	
}
