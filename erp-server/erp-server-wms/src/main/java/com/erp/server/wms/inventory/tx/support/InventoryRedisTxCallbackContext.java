package com.erp.server.wms.inventory.tx.support;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * 同事务线程内 Redis 库存回调与未分配共享锁解锁的协调上下文。
 * <p>afterCompletion 按 order 升序执行：Redis 回调(100) 先写结果，共享锁解锁(200) 再读取。</p>
 * <p>XA 路径 {@link io.seata.rm.datasource.xa.InventoryXAUtil} 依次调用实体、虚拟 {@code doXa}，须两者均成功才释放共享锁。</p>
 */
public final class InventoryRedisTxCallbackContext {

    private static final ThreadLocal<Boolean> ENTITY_CALLBACK_SUCCESS = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> VIRTUAL_CALLBACK_SUCCESS = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ENTITY_CALLBACK_REQUIRED = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> VIRTUAL_CALLBACK_REQUIRED = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> REGISTERED_REDIS_TRANSACTION_IDS = new ThreadLocal<>();

    private InventoryRedisTxCallbackContext() {
    }

    /**
     * 记录实体仓 Redis commit/rollback 回调是否成功。
     *
     * @param success true 表示成功或无需执行
     */
    public static void setEntityCallbackSuccess(boolean success) {
        ENTITY_CALLBACK_SUCCESS.set(success);
    }

    /**
     * 记录虚拟仓 Redis commit/rollback 回调是否成功。
     *
     * @param success true 表示成功或无需执行
     */
    public static void setVirtualCallbackSuccess(boolean success) {
        VIRTUAL_CALLBACK_SUCCESS.set(success);
    }

    /**
     * 标记本事务已登记实体仓 Redis 回调，解锁前须收到明确成功结果。
     */
    public static void markEntityCallbackRequired() {
        ENTITY_CALLBACK_REQUIRED.set(true);
    }

    /**
     * 标记本事务已登记虚拟仓 Redis 回调，解锁前须收到明确成功结果。
     */
    public static void markVirtualCallbackRequired() {
        VIRTUAL_CALLBACK_REQUIRED.set(true);
    }

    /**
     * 登记本事务关联的 Redis 库存 transactionId，供本地锁补偿与 Job 解锁前校验 Redis 补偿状态。
     *
     * @param transactionId Redis 事务 ID（已归一化或可含 {@code :}，内部会转为下划线形式）
     */
    public static void registerRedisTransactionId(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return;
        }
        String normalizedId = transactionId.replace(":", "_");
        Set<String> ids = REGISTERED_REDIS_TRANSACTION_IDS.get();
        if (ids == null) {
            ids = new LinkedHashSet<>();
            REGISTERED_REDIS_TRANSACTION_IDS.set(ids);
        }
        ids.add(normalizedId);
    }

    /**
     * 获取本事务已登记的全部 Redis 库存 transactionId（实体仓/虚拟仓合并）。
     *
     * @return 不可变集合；无登记时返回空集合
     */
    public static Set<String> getRegisteredRedisTransactionIds() {
        Set<String> ids = REGISTERED_REDIS_TRANSACTION_IDS.get();
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(ids));
    }

    /**
     * 本地 Spring 事务路径：任一已登记回调失败则不可释放未分配共享锁；
     * 已登记但未收到成功结果的亦不可释放。
     *
     * @return true 表示可释放未分配共享锁
     */
    public static boolean isCallbackSuccess() {
        if (Boolean.FALSE.equals(ENTITY_CALLBACK_SUCCESS.get())) {
            return false;
        }
        if (Boolean.FALSE.equals(VIRTUAL_CALLBACK_SUCCESS.get())) {
            return false;
        }
        if (Boolean.TRUE.equals(ENTITY_CALLBACK_REQUIRED.get())
                && !Boolean.TRUE.equals(ENTITY_CALLBACK_SUCCESS.get())) {
            return false;
        }
        if (Boolean.TRUE.equals(VIRTUAL_CALLBACK_REQUIRED.get())
                && !Boolean.TRUE.equals(VIRTUAL_CALLBACK_SUCCESS.get())) {
            return false;
        }
        return true;
    }

    /**
     * XA 最终回调路径：实体仓与虚拟仓 Redis 回调均须成功才释放未分配共享锁。
     *
     * @return true 表示可释放未分配共享锁
     */
    public static boolean isXaLockReleaseAllowed() {
        if (Boolean.FALSE.equals(ENTITY_CALLBACK_SUCCESS.get())) {
            return false;
        }
        if (Boolean.FALSE.equals(VIRTUAL_CALLBACK_SUCCESS.get())) {
            return false;
        }
        return Boolean.TRUE.equals(ENTITY_CALLBACK_SUCCESS.get())
                && Boolean.TRUE.equals(VIRTUAL_CALLBACK_SUCCESS.get());
    }

    /**
     * 清理线程上下文，避免线程池复用污染。
     */
    public static void clear() {
        ENTITY_CALLBACK_SUCCESS.remove();
        VIRTUAL_CALLBACK_SUCCESS.remove();
        ENTITY_CALLBACK_REQUIRED.remove();
        VIRTUAL_CALLBACK_REQUIRED.remove();
        REGISTERED_REDIS_TRANSACTION_IDS.remove();
    }
}
