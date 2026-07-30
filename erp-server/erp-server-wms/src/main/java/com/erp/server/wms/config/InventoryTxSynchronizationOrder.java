package com.erp.server.wms.config;

/**
 * Spring 事务同步器优先级：{@code getSynchronizations()} 按 order 升序排序，
 * {@code afterCompletion} 正序遍历，因此 order 更小者先执行。
 * <p>Redis commit/rollback 须先于未分配共享锁解锁。</p>
 */
public final class InventoryTxSynchronizationOrder {

    /** Redis 库存事务 commit/rollback 回调（须先于解锁执行） */
    public static final int REDIS_TX_CALLBACK = 100;

    /** 未分配仓+SKU 共享锁解锁（须在 Redis 回调之后） */
    public static final int UNALLOC_LOCK_UNLOCK = 200;

    private InventoryTxSynchronizationOrder() {
    }
}
