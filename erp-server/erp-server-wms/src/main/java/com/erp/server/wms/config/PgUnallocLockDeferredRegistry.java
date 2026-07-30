package com.erp.server.wms.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Seata 全局事务下未分配共享锁延迟释放：全局事务执行期登记至内存等待 XA 回调；
 * XA 解锁失败时优先写入 Redis 补偿，Redis 不可用时回退本机内存；Job 扫描 Redis + 本机失败登记。
 */
@Slf4j
public final class PgUnallocLockDeferredRegistry {

    /** 等待 XA 最终回调释放（仅本 JVM，Job 不扫描） */
    private static final ConcurrentMap<String, Map<String, Long>> WAITING_XA_UNLOCKS = new ConcurrentHashMap<>();

    /** XA 已完成但解锁失败且 Redis 登记失败时的本机补偿回退（Job 扫描） */
    private static final ConcurrentMap<String, Map<String, Long>> LOCAL_RETRY_UNLOCKS = new ConcurrentHashMap<>();

    private static final String RETRY_KEY_PREFIX = "inventory:unalloc:lock:retry";

    /** XA 解锁失败补偿登记 TTL（秒） */
    private static final long RETRY_KEY_TTL_SECONDS = 86400L;

    private PgUnallocLockDeferredRegistry() {
    }

    /**
     * 登记待 XA 释放的未分配共享锁（按全局事务 XID）；同一 XID 多次登记会合并 lock key 并保留首次 threadId。
     *
     * @param transactionId 全局事务 ID（Seata XID，已归一化为下划线形式）
     * @param lockKeys        未分配共享锁 Redis key 列表
     * @param threadId        加锁时 {@link Thread#getId()}
     */
    public static void register(String transactionId, List<String> lockKeys, long threadId) {
        if (StringUtils.isBlank(transactionId) || CollUtil.isEmpty(lockKeys)) {
            return;
        }
        String normalizedId = normalizeTransactionId(transactionId);
        WAITING_XA_UNLOCKS.compute(normalizedId, (id, existing) -> {
            Map<String, Long> merged = existing != null ? new LinkedHashMap<>(existing) : new LinkedHashMap<>();
            for (String lockKey : lockKeys) {
                if (StringUtils.isNotBlank(lockKey)) {
                    merged.putIfAbsent(lockKey, threadId);
                }
            }
            return merged.isEmpty() ? null : merged;
        });
    }

    /**
     * XA 最终完成时释放登记锁；仅在解锁成功或补偿登记成功（Redis 或本机回退）后清除等待登记。
     *
     * @param transactionId 全局事务 ID
     */
    public static void releaseByTransactionId(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return;
        }
        String normalizedId = normalizeTransactionId(transactionId);
        Map<String, Long> lockKeyToThreadId = mergeRetrySources(normalizedId, true);
        if (CollUtil.isEmpty(lockKeyToThreadId)) {
            return;
        }
        attemptUnlockAndPersist(normalizedId, lockKeyToThreadId, true, "XA");
    }

    /**
     * 重试 XA 解锁失败的未分配共享锁（Redis 补偿 + 本机回退）；不扫描等待 XA 的内存登记。
     *
     * @return 仍存在未释放锁的 transactionId 数量
     */
    public static int retryAllPendingUnlocks() {
        Set<String> transactionIds = collectRetryTransactionIds();
        if (CollUtil.isEmpty(transactionIds)) {
            return 0;
        }
        int remaining = 0;
        for (String normalizedId : transactionIds) {
            Map<String, Long> lockKeyToThreadId = mergeRetrySources(normalizedId, false);
            if (CollUtil.isEmpty(lockKeyToThreadId)) {
                clearAllRetryStores(normalizedId);
                continue;
            }
            if (attemptUnlockAndPersist(normalizedId, lockKeyToThreadId, false, "Job")) {
                remaining++;
            }
        }
        return remaining;
    }

    private static Set<String> collectRetryTransactionIds() {
        Set<String> transactionIds = new HashSet<>(LOCAL_RETRY_UNLOCKS.keySet());
        Collection<String> retryKeys = resolveRedisUtil().keys(RETRY_KEY_PREFIX + ":*");
        if (CollUtil.isNotEmpty(retryKeys)) {
            for (String retryKey : retryKeys) {
                String normalizedId = extractTransactionId(retryKey);
                if (StringUtils.isNotBlank(normalizedId)) {
                    transactionIds.add(normalizedId);
                }
            }
        }
        return transactionIds;
    }

    /**
     * @param includeWaiting true 时合并 {@link #WAITING_XA_UNLOCKS}（仅 XA 回调使用）
     */
    private static Map<String, Long> mergeRetrySources(String normalizedId, boolean includeWaiting) {
        Map<String, Long> merged = new LinkedHashMap<>();
        if (includeWaiting) {
            Map<String, Long> waitingLocks = WAITING_XA_UNLOCKS.get(normalizedId);
            if (CollUtil.isNotEmpty(waitingLocks)) {
                merged.putAll(waitingLocks);
            }
        }
        Map<String, Long> localRetry = LOCAL_RETRY_UNLOCKS.get(normalizedId);
        if (CollUtil.isNotEmpty(localRetry)) {
            localRetry.forEach(merged::putIfAbsent);
        }
        Map<String, Long> redisRetry = loadRetryLockMap(normalizedId);
        if (CollUtil.isNotEmpty(redisRetry)) {
            redisRetry.forEach(merged::putIfAbsent);
        }
        return merged;
    }

    /**
     * 尝试按 threadId 解锁，并将失败项持久化到 Redis；Redis 失败时写入本机回退 Map。
     *
     * @param removeWaitingAfterXa true 表示 XA 回调路径，成功后清除等待登记
     * @return true 表示仍有未释放锁
     */
    private static boolean attemptUnlockAndPersist(String normalizedId, Map<String, Long> lockKeyToThreadId,
                                                   boolean removeWaitingAfterXa, String phase) {
        Map<String, Long> failed = doUnlockByThreadId(lockKeyToThreadId);
        if (CollUtil.isEmpty(failed)) {
            if (removeWaitingAfterXa) {
                WAITING_XA_UNLOCKS.remove(normalizedId);
            }
            clearAllRetryStores(normalizedId);
            log.warn("{} 释放未分配共享锁成功 transactionId={} lockCount={}", phase, normalizedId, lockKeyToThreadId.size());
            return false;
        }
        persistFailedUnlocks(normalizedId, failed);
        if (removeWaitingAfterXa) {
            WAITING_XA_UNLOCKS.remove(normalizedId);
        }
        log.error("{} 释放未分配共享锁部分失败 transactionId={} failedCount={} totalCount={}",
                phase, normalizedId, failed.size(), lockKeyToThreadId.size());
        return true;
    }

    /**
     * 将解锁失败项写入 Redis；Redis 不可用时回退至本机 {@link #LOCAL_RETRY_UNLOCKS}。
     *
     * @param normalizedId   归一化全局事务 ID
     * @param failedLockKeys 解锁失败的 lock key 及 threadId
     */
    private static void persistFailedUnlocks(String normalizedId, Map<String, Long> failedLockKeys) {
        if (CollUtil.isEmpty(failedLockKeys)) {
            clearAllRetryStores(normalizedId);
            return;
        }
        if (saveRetryLockMapSafely(normalizedId, failedLockKeys)) {
            LOCAL_RETRY_UNLOCKS.remove(normalizedId);
            return;
        }
        LOCAL_RETRY_UNLOCKS.put(normalizedId, new LinkedHashMap<>(failedLockKeys));
        log.warn("未分配共享锁 Redis 补偿登记失败，回退本机重试 transactionId={} lockCount={}",
                normalizedId, failedLockKeys.size());
    }

    private static void clearAllRetryStores(String normalizedId) {
        LOCAL_RETRY_UNLOCKS.remove(normalizedId);
        clearRetryLockMap(normalizedId);
    }

    private static Map<String, Long> doUnlockByThreadId(Map<String, Long> lockKeyToThreadId) {
        try {
            return ApplicationContextUtils.getBean(InventoryRedisUtil.class)
                    .unlockByThreadId(new LinkedHashMap<>(lockKeyToThreadId));
        } catch (Exception e) {
            log.error("释放未分配共享锁失败 lockCount={}", lockKeyToThreadId.size(), e);
            return new LinkedHashMap<>(lockKeyToThreadId);
        }
    }

    private static boolean saveRetryLockMapSafely(String normalizedId, Map<String, Long> lockKeyToThreadId) {
        try {
            saveRetryLockMap(normalizedId, lockKeyToThreadId);
            return true;
        } catch (Exception e) {
            log.error("未分配共享锁 Redis 补偿登记失败 transactionId={} lockCount={}",
                    normalizedId, lockKeyToThreadId.size(), e);
            return false;
        }
    }

    private static void saveRetryLockMap(String normalizedId, Map<String, Long> lockKeyToThreadId) {
        if (CollUtil.isEmpty(lockKeyToThreadId)) {
            clearRetryLockMap(normalizedId);
            return;
        }
        resolveRedisUtil().set(buildRetryKey(normalizedId), JSON.toJSONString(lockKeyToThreadId), RETRY_KEY_TTL_SECONDS);
    }

    private static Map<String, Long> loadRetryLockMap(String normalizedId) {
        Object value = resolveRedisUtil().get(buildRetryKey(normalizedId));
        if (value == null || CharSequenceUtil.isBlank(value.toString())) {
            return new LinkedHashMap<>();
        }
        try {
            JSONObject jsonObject = JSON.parseObject(value.toString());
            Map<String, Long> lockKeyToThreadId = new LinkedHashMap<>();
            for (String lockKey : jsonObject.keySet()) {
                lockKeyToThreadId.put(lockKey, jsonObject.getLongValue(lockKey));
            }
            return lockKeyToThreadId;
        } catch (Exception e) {
            log.error("解析未分配共享锁补偿登记失败 transactionId={} value={}", normalizedId, value, e);
            return new LinkedHashMap<>();
        }
    }

    private static void clearRetryLockMap(String normalizedId) {
        resolveRedisUtil().del(buildRetryKey(normalizedId));
    }

    private static String buildRetryKey(String normalizedId) {
        return RETRY_KEY_PREFIX + ":" + normalizedId;
    }

    private static String extractTransactionId(String retryKey) {
        if (StringUtils.isBlank(retryKey) || !retryKey.startsWith(RETRY_KEY_PREFIX + ":")) {
            return "";
        }
        return retryKey.substring(RETRY_KEY_PREFIX.length() + 1);
    }

    private static InventoryRedisUtil resolveRedisUtil() {
        return ApplicationContextUtils.getBean(InventoryRedisUtil.class);
    }

    private static String normalizeTransactionId(String transactionId) {
        return transactionId == null ? "" : transactionId.replace(":", "_");
    }
}
