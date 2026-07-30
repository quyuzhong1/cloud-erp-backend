package com.erp.server.wms.inventory.tx.sync;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.erp.server.wms.inventory.tx.lock.PgUnallocLockDeferredRegistry;
import com.erp.server.wms.inventory.tx.support.InventoryRedisTxCallbackContext;

import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		InventoryRedisTxCallbackContext.markVirtualCallbackRequired();
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
	
	/**
	 * Seata XA 回调：虚拟库存 Redis 提交/回滚完成后，按回调结果决定是否释放未分配共享锁。
	 * {@link io.seata.rm.datasource.xa.InventoryXAUtil} 先调实体 adapter 再调本方法，锁须在此处统一释放。
	 *
	 * @param transactionId 归一化全局事务 ID
	 * @param status          事务完成状态
	 */
	public void doXa(String transactionId , Integer status) {
		this.transactionId = transactionId;
		try {
			InventoryRedisTxSynchronizationHelper.afterVirtualCompletion(transactionId, status, true);
			if (InventoryRedisTxCallbackContext.isXaLockReleaseAllowed()) {
				PgUnallocLockDeferredRegistry.releaseByTransactionId(transactionId);
			} else {
				log.warn("XA Redis 实体/虚拟回调未全部成功，保留未分配共享锁并登记补偿 transactionId={}", transactionId);
				PgUnallocLockDeferredRegistry.promoteWaitingToRetry(transactionId);
			}
		} finally {
			InventoryRedisTxCallbackContext.clear();
		}
	}
	
}
