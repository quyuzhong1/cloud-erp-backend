package com.erp.server.wms.config;

import java.util.List;

import org.redisson.RedissonMultiLock;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.collection.CollUtil;
import io.seata.core.context.RootContext;

/**
 * PG 路径未分配仓+SKU 锁：本地 Spring 事务提交或回滚后再释放；全局事务下延迟至 XA 最终回调释放。
 */
public class PgUnallocLockSynchronizationAdapter extends TransactionSynchronizationAdapter {

    private final RedissonMultiLock lock;
    private final InventoryRedisUtil inventoryRedisUtil;

    /**
     * @param lock               已获取的仓+SKU MultiLock
     * @param inventoryRedisUtil Redis 锁工具
     */
    public PgUnallocLockSynchronizationAdapter(RedissonMultiLock lock, InventoryRedisUtil inventoryRedisUtil) {
        this.lock = lock;
        this.inventoryRedisUtil = inventoryRedisUtil;
    }

    @Override
    public void afterCompletion(int status) {
        inventoryRedisUtil.unLock(lock);
    }

    @Override
    public int getOrder() {
        // afterCompletion 升序执行：order 更大者后解锁，确保 Redis commit 完成后再释放共享锁
        return InventoryTxSynchronizationOrder.UNALLOC_LOCK_UNLOCK;
    }

    /**
     * 注册事务结束后解锁；无活跃 Spring 事务时由调用方在 {@code try/finally} 中释放锁。
     * 全局事务下不在本地 afterCompletion 解锁，登记至 {@link PgUnallocLockDeferredRegistry} 待 XA 回调释放。
     *
     * @param lock               已获取的锁，可为 null
     * @param inventoryRedisUtil Redis 锁工具
     * @param lockKeys           未分配共享锁 Redis key 列表（全局事务延迟释放时使用）
     */
    public static void registerUnlockAfterTx(RedissonMultiLock lock, InventoryRedisUtil inventoryRedisUtil,
                                             List<String> lockKeys) {
        if (lock == null || inventoryRedisUtil == null || CollUtil.isEmpty(lockKeys)) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        if (RootContext.inGlobalTransaction()) {
            String transactionId = RootContext.getXID().replace(":", "_");
            PgUnallocLockDeferredRegistry.register(transactionId, lockKeys, Thread.currentThread().getId());
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new PgUnallocLockSynchronizationAdapter(lock, inventoryRedisUtil));
    }
}
