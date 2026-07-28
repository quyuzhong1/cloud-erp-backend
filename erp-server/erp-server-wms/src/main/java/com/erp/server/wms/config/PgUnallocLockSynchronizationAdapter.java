package com.erp.server.wms.config;

import org.redisson.RedissonMultiLock;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.erp.server.wms.utils.InventoryRedisUtil;

/**
 * PG 路径未分配仓+SKU 锁：在 Spring 事务提交或回滚后再释放，避免锁早于 DB commit 导致并发穿透。
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

    /**
     * 注册事务结束后解锁；无活跃事务时立即释放。
     *
     * @param lock               已获取的锁，可为 null
     * @param inventoryRedisUtil Redis 锁工具
     */
    public static void registerUnlockAfterTx(RedissonMultiLock lock, InventoryRedisUtil inventoryRedisUtil) {
        if (lock == null || inventoryRedisUtil == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new PgUnallocLockSynchronizationAdapter(lock, inventoryRedisUtil));
        } else {
            inventoryRedisUtil.unLock(lock);
        }
    }
}
