package com.erp.server.wms.inventory.tx.lock;

import java.util.List;

import org.redisson.RedissonMultiLock;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.erp.server.wms.inventory.tx.sync.InventoryTxSynchronizationOrder;
import com.erp.server.wms.inventory.tx.support.InventoryRedisTxCallbackContext;
import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.collection.CollUtil;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;

/**
 * PG 路径未分配仓+SKU 锁：本地 Spring 事务提交或回滚后再释放；全局事务下延迟至 XA 最终回调释放。
 */
@Slf4j
public class PgUnallocLockSynchronizationAdapter extends TransactionSynchronizationAdapter {

    private final RedissonMultiLock lock;
    private final InventoryRedisUtil inventoryRedisUtil;
    private final List<String> lockKeys;
    private final long threadId;

    /**
     * @param lock               已获取的仓+SKU MultiLock
     * @param inventoryRedisUtil Redis 锁工具
     * @param lockKeys           未分配共享锁 Redis key 列表（解锁失败补偿用）
     * @param threadId           加锁时 {@link Thread#getId()}
     */
    public PgUnallocLockSynchronizationAdapter(RedissonMultiLock lock, InventoryRedisUtil inventoryRedisUtil,
                                               List<String> lockKeys, long threadId) {
        this.lock = lock;
        this.inventoryRedisUtil = inventoryRedisUtil;
        this.lockKeys = lockKeys;
        this.threadId = threadId;
    }

    /**
     * Redis 回调成功后再释放共享锁；Redis 回调失败或 unlock 异常时登记补偿，避免提前释放临界区。
     */
    @Override
    public void afterCompletion(int status) {
        try {
            if (!InventoryRedisTxCallbackContext.isCallbackSuccess()) {
                log.warn("Redis 库存回调失败，保留未分配共享锁并登记补偿 lockCount={}", CollUtil.size(lockKeys));
                PgUnallocLockDeferredRegistry.registerLocalRetryUnlock(lockKeys, threadId);
                return;
            }
            inventoryRedisUtil.unLock(lock);
        } catch (Exception e) {
            log.warn("本地未分配共享锁 unlock 失败，登记补偿 lockCount={}", CollUtil.size(lockKeys), e);
            PgUnallocLockDeferredRegistry.registerLocalRetryUnlock(lockKeys, threadId);
        }
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
        long threadId = Thread.currentThread().getId();
        if (RootContext.inGlobalTransaction()) {
            String transactionId = RootContext.getXID().replace(":", "_");
            PgUnallocLockDeferredRegistry.register(transactionId, lockKeys, threadId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new PgUnallocLockSynchronizationAdapter(lock, inventoryRedisUtil, lockKeys, threadId));
    }
}
