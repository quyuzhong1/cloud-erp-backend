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
        MergeRetryResult mergeResult = mergeRetrySources(normalizedId, true);
        if (CollUtil.isEmpty(mergeResult.getLockKeyToThreadId())) {
            if (mergeResult.isRedisReadFailed()) {
                log.warn("XA 释放时 Redis 补偿登记读取失败且无本机登记，保留 Redis transactionId={}", normalizedId);
            }
            return;
        }
        attemptUnlockAndPersist(normalizedId, mergeResult.getLockKeyToThreadId(), true, "XA",
                mergeResult.isRedisReadFailed());
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
            MergeRetryResult mergeResult = mergeRetrySources(normalizedId, false);
            if (CollUtil.isEmpty(mergeResult.getLockKeyToThreadId())) {
                if (mergeResult.isRedisReadFailed()) {
                    log.warn("Job 重试时 Redis 补偿登记读取失败且无本机登记，保留 Redis transactionId={}", normalizedId);
                    remaining++;
                } else {
                    clearAllRetryStores(normalizedId);
                }
                continue;
            }
            boolean stillPending = attemptUnlockAndPersist(normalizedId, mergeResult.getLockKeyToThreadId(), false, "Job",
                    mergeResult.isRedisReadFailed());
            if (stillPending || mergeResult.isRedisReadFailed()) {
                remaining++;
            }
        }
        return remaining;
    }

    private static Set<String> collectRetryTransactionIds() {
        Set<String> transactionIds = new HashSet<>(LOCAL_RETRY_UNLOCKS.keySet());
        try {
            Collection<String> retryKeys = resolveRedisUtil().keys(RETRY_KEY_PREFIX + ":*");
            if (CollUtil.isNotEmpty(retryKeys)) {
                for (String retryKey : retryKeys) {
                    String normalizedId = extractTransactionId(retryKey);
                    if (StringUtils.isNotBlank(normalizedId)) {
                        transactionIds.add(normalizedId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("未分配共享锁 Redis keys 查询失败，仅扫描本机补偿 transactionId", e);
        }
        return transactionIds;
    }

    /**
     * 合并本机等待/回退登记与 Redis 补偿登记。
     *
     * @param includeWaiting true 时合并 {@link #WAITING_XA_UNLOCKS}（仅 XA 回调使用）
     * @return 合并结果；{@link MergeRetryResult#isRedisReadFailed()} 为 true 且合并为空时，不得清理 Redis 登记
     */
    private static MergeRetryResult mergeRetrySources(String normalizedId, boolean includeWaiting) {
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
        RetryLockMapLoadResult redisResult = loadRetryLockMapSafely(normalizedId);
        if (!redisResult.isReadSuccess()) {
            return new MergeRetryResult(merged, true);
        }
        if (CollUtil.isNotEmpty(redisResult.getLockKeyToThreadId())) {
            redisResult.getLockKeyToThreadId().forEach(merged::putIfAbsent);
        }
        return new MergeRetryResult(merged, false);
    }

    /**
     * 尝试按 threadId 解锁，并将失败项持久化到 Redis；Redis 失败时写入本机回退 Map。
     *
     * @param removeWaitingAfterXa true 表示 XA 回调路径，成功后清除等待登记
     * @param redisReadFailed      true 表示 Redis 登记未成功读取，禁止 del/set Redis 补偿 key
     * @return true 表示仍有未释放锁
     */
    private static boolean attemptUnlockAndPersist(String normalizedId, Map<String, Long> lockKeyToThreadId,
                                                   boolean removeWaitingAfterXa, String phase,
                                                   boolean redisReadFailed) {
        Map<String, Long> failed = doUnlockByThreadId(lockKeyToThreadId);
        if (CollUtil.isEmpty(failed)) {
            if (removeWaitingAfterXa) {
                WAITING_XA_UNLOCKS.remove(normalizedId);
            }
            if (redisReadFailed) {
                clearLocalRetryStoresOnly(normalizedId);
                log.warn("{} 释放未分配共享锁成功(跳过Redis清理) transactionId={} lockCount={}",
                        phase, normalizedId, lockKeyToThreadId.size());
            } else {
                clearAllRetryStores(normalizedId);
                log.warn("{} 释放未分配共享锁成功 transactionId={} lockCount={}",
                        phase, normalizedId, lockKeyToThreadId.size());
            }
            return false;
        }
        persistFailedUnlocks(normalizedId, failed, redisReadFailed);
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
     * @param redisReadFailed true 时仅更新本机回退，禁止覆盖 Redis 登记
     */
    private static void persistFailedUnlocks(String normalizedId, Map<String, Long> failedLockKeys,
                                             boolean redisReadFailed) {
        if (CollUtil.isEmpty(failedLockKeys)) {
            if (redisReadFailed) {
                clearLocalRetryStoresOnly(normalizedId);
            } else {
                clearAllRetryStores(normalizedId);
            }
            return;
        }
        if (redisReadFailed) {
            LOCAL_RETRY_UNLOCKS.put(normalizedId, new LinkedHashMap<>(failedLockKeys));
            log.warn("未分配共享锁 Redis 读失败，仅回写本机补偿 transactionId={} lockCount={}",
                    normalizedId, failedLockKeys.size());
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

    /**
     * 仅清除本机补偿登记，不修改 Redis。
     *
     * @param normalizedId 归一化全局事务 ID
     */
    private static void clearLocalRetryStoresOnly(String normalizedId) {
        LOCAL_RETRY_UNLOCKS.remove(normalizedId);
    }

    private static void clearAllRetryStores(String normalizedId) {
        LOCAL_RETRY_UNLOCKS.remove(normalizedId);
        clearRetryLockMapSafely(normalizedId);
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
            clearRetryLockMapSafely(normalizedId);
            return;
        }
        resolveRedisUtil().set(buildRetryKey(normalizedId), JSON.toJSONString(lockKeyToThreadId), RETRY_KEY_TTL_SECONDS);
    }

    /**
     * 读取 Redis 补偿登记；基础设施异常或 JSON 解析失败时 {@link RetryLockMapLoadResult#isReadSuccess()} 为 false。
     *
     * @param normalizedId 归一化全局事务 ID
     * @return 读取结果，禁止将 readSuccess=false 当作「无登记」
     */
    private static RetryLockMapLoadResult loadRetryLockMapSafely(String normalizedId) {
        try {
            return loadRetryLockMap(normalizedId);
        } catch (Exception e) {
            log.warn("未分配共享锁 Redis 补偿登记读取失败 transactionId={}", normalizedId, e);
            return RetryLockMapLoadResult.readFailed();
        }
    }

    /**
     * 从 Redis 读取单条补偿登记。
     *
     * @param normalizedId 归一化全局事务 ID
     * @return key 不存在时 readSuccess=true 且空 Map；解析失败时 readSuccess=false
     */
    private static RetryLockMapLoadResult loadRetryLockMap(String normalizedId) {
        Object value = resolveRedisUtil().get(buildRetryKey(normalizedId));
        if (value == null || CharSequenceUtil.isBlank(value.toString())) {
            return RetryLockMapLoadResult.success(new LinkedHashMap<>());
        }
        try {
            JSONObject jsonObject = JSON.parseObject(value.toString());
            Map<String, Long> lockKeyToThreadId = new LinkedHashMap<>();
            for (String lockKey : jsonObject.keySet()) {
                lockKeyToThreadId.put(lockKey, jsonObject.getLongValue(lockKey));
            }
            return RetryLockMapLoadResult.success(lockKeyToThreadId);
        } catch (Exception e) {
            log.error("解析未分配共享锁补偿登记失败 transactionId={} value={}", normalizedId, value, e);
            return RetryLockMapLoadResult.readFailed();
        }
    }

    private static void clearRetryLockMapSafely(String normalizedId) {
        try {
            resolveRedisUtil().del(buildRetryKey(normalizedId));
        } catch (Exception e) {
            log.warn("清除未分配共享锁 Redis 补偿登记失败 transactionId={}", normalizedId, e);
        }
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

    /**
     * Redis 补偿登记读取结果，区分「无登记」与「读取/解析失败」。
     */
    private static final class RetryLockMapLoadResult {

        private final Map<String, Long> lockKeyToThreadId;
        private final boolean readSuccess;

        private RetryLockMapLoadResult(Map<String, Long> lockKeyToThreadId, boolean readSuccess) {
            this.lockKeyToThreadId = lockKeyToThreadId;
            this.readSuccess = readSuccess;
        }

        /**
         * @param lockKeyToThreadId 登记内容，允许为空 Map
         * @return readSuccess=true 的读取结果
         */
        static RetryLockMapLoadResult success(Map<String, Long> lockKeyToThreadId) {
            return new RetryLockMapLoadResult(lockKeyToThreadId, true);
        }

        /**
         * @return readSuccess=false，调用方不得据此清理 Redis 登记
         */
        static RetryLockMapLoadResult readFailed() {
            return new RetryLockMapLoadResult(new LinkedHashMap<>(), false);
        }

        Map<String, Long> getLockKeyToThreadId() {
            return lockKeyToThreadId;
        }

        boolean isReadSuccess() {
            return readSuccess;
        }
    }

    /**
     * 合并多来源补偿登记的结果；redisReadFailed 为 true 且 lockKeyToThreadId 为空时禁止清理 Redis。
     */
    private static final class MergeRetryResult {

        private final Map<String, Long> lockKeyToThreadId;
        private final boolean redisReadFailed;

        private MergeRetryResult(Map<String, Long> lockKeyToThreadId, boolean redisReadFailed) {
            this.lockKeyToThreadId = lockKeyToThreadId;
            this.redisReadFailed = redisReadFailed;
        }

        Map<String, Long> getLockKeyToThreadId() {
            return lockKeyToThreadId;
        }

        boolean isRedisReadFailed() {
            return redisReadFailed;
        }
    }
}
