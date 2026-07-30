package com.erp.server.wms.inventory.tx.sync;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.erp.server.wms.inventory.tx.support.InventoryRedisTxCallbackContext;

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
		InventoryRedisTxSynchronizationHelper.afterEntityCompletion(transactionId, status, needCommit);
	}

	/**
	 * XA 最终回调：执行 Redis commit/rollback。
	 *
	 * @param transactionId 归一化全局事务 ID
	 * @param status          事务完成状态
	 * @return true 表示 Redis 回调成功或无需执行
	 */
	public boolean doXaWithResult(String transactionId, Integer status) {
		InventoryRedisTxCallbackContext.clear();
		this.transactionId = transactionId;
		return InventoryRedisTxSynchronizationHelper.afterEntityCompletion(transactionId, status, true);
	}

	@Override
	public int getOrder() {
		// afterCompletion 升序执行：order 更小者先跑 Redis commit/rollback
		return InventoryTxSynchronizationOrder.REDIS_TX_CALLBACK;
	}
	
	public static void register(String transactionId) {
		InventoryTransactionSynchronizationAdapter synchronization = new InventoryTransactionSynchronizationAdapter();
		synchronization.setTransactionId(transactionId);
		synchronization.setNeedCommit(!RootContext.inGlobalTransaction());
		InventoryRedisTxCallbackContext.markEntityCallbackRequired();
		InventoryRedisTxCallbackContext.registerRedisTransactionId(transactionId);
		if(TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(synchronization);
			registerCallbackContextCleanup();
		}else {
			synchronization.afterCompletion(STATUS_COMMITTED);
			InventoryRedisTxCallbackContext.clear();
		}
	}

	/**
	 * 事务完成后清理 Redis 回调上下文，避免线程池复用污染。
	 */
	private static void registerCallbackContextCleanup() {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public int getOrder() {
				return InventoryTxSynchronizationOrder.UNALLOC_LOCK_UNLOCK + 1;
			}

			@Override
			public void afterCompletion(int status) {
				InventoryRedisTxCallbackContext.clear();
			}
		});
	}
	
	public void doXa(String transactionId , Integer status) {
		doXaWithResult(transactionId, status);
	}
	
}
