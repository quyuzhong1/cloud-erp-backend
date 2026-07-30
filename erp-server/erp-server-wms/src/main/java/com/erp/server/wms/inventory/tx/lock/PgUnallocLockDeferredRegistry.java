package com.erp.server.wms.inventory.tx.lock;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.server.wms.inventory.tx.compensate.InventoryRedisTxCompensateRegistry;
import com.erp.server.wms.inventory.tx.support.InventoryRedisTxCallbackContext;
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

    private static final String JSON_LOCKS = "locks";
    private static final String JSON_REDIS_TX_IDS = "redisTransactionIds";

    /** 等待 XA 最终回调释放（仅本 JVM，Job 不扫描） */
    private static final ConcurrentMap<String, Map<String, Long>> WAITING_XA_UNLOCKS = new ConcurrentHashMap<>();

    /** XA 已完成但解锁失败且 Redis 登记失败时的本机补偿回退（Job 扫描） */
    private static final ConcurrentMap<String, RetryUnlockPayload> LOCAL_RETRY_UNLOCKS = new ConcurrentHashMap<>();

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
     * Redis 回调失败时，将等待 XA 释放的登记转入补偿重试存储。
     *
     * @param transactionId 全局事务 ID
     */
    public static void promoteWaitingToRetry(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            return;
        }
        String normalizedId = normalizeTransactionId(transactionId);
        Map<String, Long> waiting = WAITING_XA_UNLOCKS.remove(normalizedId);
        if (CollUtil.isEmpty(waiting)) {
            return;
        }
        persistFailedUnlocks(normalizedId, RetryUnlockPayload.ofLocks(waiting), false);
        log.warn("Redis 回调失败，未分配共享锁 WAITING 转入补偿登记 transactionId={} lockCount={}",
                normalizedId, waiting.size());
    }

    /**
     * 本地 Spring 事务路径：Redis 回调失败或 unlock 异常时登记补偿，并绑定关联 Redis transactionId。
     *
     * @param lockKeys 未分配共享锁 Redis key 列表
     * @param threadId 加锁时 {@link Thread#getId()}
     */
    public static void registerLocalRetryUnlock(List<String> lockKeys, long threadId) {
        if (CollUtil.isEmpty(lockKeys)) {
            return;
        }
        Map<String, Long> lockKeyToThreadId = new LinkedHashMap<>();
        for (String lockKey : lockKeys) {
            if (StringUtils.isNotBlank(lockKey)) {
                lockKeyToThreadId.putIfAbsent(lockKey, threadId);
            }
        }
        if (lockKeyToThreadId.isEmpty()) {
            return;
        }
        String localRetryId = buildLocalRetryId(lockKeyToThreadId.keySet());
        RetryUnlockPayload incoming = RetryUnlockPayload.ofLocal(lockKeyToThreadId,
                InventoryRedisTxCallbackContext.getRegisteredRedisTransactionIds());
        MergeRetryResult existing = mergeRetrySources(localRetryId, false);
        RetryUnlockPayload merged = existing.getPayload().merge(incoming);
        persistFailedUnlocks(localRetryId, merged, existing.isRedisReadFailed());
        log.warn("本地未分配共享锁解锁失败，转入补偿登记 retryId={} lockCount={} redisTxIds={}",
                localRetryId, lockKeyToThreadId.size(), merged.getRedisTransactionIds());
    }

    /**
     * 为本地补偿生成稳定 retryId（按 lock key 排序后哈希）。
     *
     * @param lockKeys 锁 key 集合
     * @return local$ 前缀 retryId
     */
    private static String buildLocalRetryId(Set<String> lockKeys) {
        String joined = lockKeys.stream().sorted().collect(java.util.stream.Collectors.joining("$"));
        return "local$" + Math.abs(joined.hashCode());
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
        if (mergeResult.getPayload().isEmpty()) {
            if (mergeResult.isRedisReadFailed()) {
                log.warn("XA 释放时 Redis 补偿登记读取失败且无本机登记，保留 Redis transactionId={}", normalizedId);
            }
            return;
        }
        attemptUnlockAndPersist(normalizedId, mergeResult.getPayload(), true, "XA",
                mergeResult.isRedisReadFailed());
    }

    /**
     * 重试 XA 解锁失败的未分配共享锁（Redis 补偿 + 本机回退）；不扫描等待 XA 的内存登记。
     *
     * @return 仍存在未释放锁的 transactionId 数量
     */
    public static int retryAllPendingUnlocks() {
        RetryTransactionIdsResult collectResult = collectRetryTransactionIds();
        Set<String> transactionIds = collectResult.getTransactionIds();
        if (CollUtil.isEmpty(transactionIds)) {
            if (collectResult.isRedisScanFailed()) {
                log.warn("Redis 补偿登记扫描失败且无本机登记，本轮无法确认全部待补偿任务");
                return 1;
            }
            return 0;
        }
        int remaining = 0;
        for (String normalizedId : transactionIds) {
            MergeRetryResult mergeResult = mergeRetrySources(normalizedId, false);
            if (shouldDeferUnlockForRedisCompensation(normalizedId, mergeResult.getPayload())) {
                log.warn("Job 锁补偿推迟：Redis 库存补偿未完成 retryId={} redisTxIds={}",
                        normalizedId, mergeResult.getPayload().getRedisTransactionIds());
                remaining++;
                continue;
            }
            if (mergeResult.getPayload().isEmpty()) {
                if (mergeResult.isRedisReadFailed()) {
                    log.warn("Job 重试时 Redis 补偿登记读取失败且无本机登记，保留 Redis transactionId={}", normalizedId);
                    remaining++;
                } else {
                    clearAllRetryStores(normalizedId);
                }
                continue;
            }
            boolean stillPending = attemptUnlockAndPersist(normalizedId, mergeResult.getPayload(), false, "Job",
                    mergeResult.isRedisReadFailed());
            if (stillPending || mergeResult.isRedisReadFailed()) {
                remaining++;
            }
        }
        if (collectResult.isRedisScanFailed()) {
            log.warn("Redis 补偿登记扫描失败，本轮无法确认全部待补偿任务 remaining={}", remaining);
            return Math.max(remaining, 1);
        }
        return remaining;
    }

    /**
     * 收集待重试的全局事务 ID（本机回退 + Redis 补偿登记）。
     *
     * @return transactionId 集合及 Redis keys 扫描是否失败；扫描失败时调用方不得将 remaining 视为 0
     */
    private static RetryTransactionIdsResult collectRetryTransactionIds() {
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
            return RetryTransactionIdsResult.success(transactionIds);
        } catch (Exception e) {
            log.warn("未分配共享锁 Redis keys 查询失败，仅扫描本机补偿 transactionId", e);
            return RetryTransactionIdsResult.scanFailed(transactionIds);
        }
    }

    /**
     * 合并本机等待/回退登记与 Redis 补偿登记。
     *
     * @param includeWaiting true 时合并 {@link #WAITING_XA_UNLOCKS}（仅 XA 回调使用）
     * @return 合并结果；{@link MergeRetryResult#isRedisReadFailed()} 为 true 且合并为空时，不得清理 Redis 登记
     */
    private static MergeRetryResult mergeRetrySources(String normalizedId, boolean includeWaiting) {
        RetryUnlockPayload merged = RetryUnlockPayload.empty();
        if (includeWaiting) {
            Map<String, Long> waitingLocks = WAITING_XA_UNLOCKS.get(normalizedId);
            if (CollUtil.isNotEmpty(waitingLocks)) {
                merged = merged.merge(RetryUnlockPayload.ofLocks(waitingLocks));
            }
        }
        RetryUnlockPayload localRetry = LOCAL_RETRY_UNLOCKS.get(normalizedId);
        if (localRetry != null && !localRetry.isEmpty()) {
            merged = merged.merge(localRetry);
        }
        RetryLockMapLoadResult redisResult = loadRetryLockMapSafely(normalizedId);
        if (!redisResult.isReadSuccess()) {
            return new MergeRetryResult(merged, true);
        }
        if (!redisResult.getPayload().isEmpty()) {
            merged = merged.merge(redisResult.getPayload());
        }
        return new MergeRetryResult(merged, false);
    }

    /**
     * Job / XA 解锁前判断关联 Redis transactionId 补偿是否仍待处理。
     *
     * @param normalizedId 补偿登记 ID（全局 XID 或 local$）
     * @param payload      合并后的补偿载荷
     * @return true 表示应推迟解锁
     */
    private static boolean shouldDeferUnlockForRedisCompensation(String normalizedId, RetryUnlockPayload payload) {
        Set<String> txIdsToCheck = new LinkedHashSet<>();
        if (isLocalRetryId(normalizedId)) {
            if (CollUtil.isEmpty(payload.getRedisTransactionIds())) {
                return false;
            }
            txIdsToCheck.addAll(payload.getRedisTransactionIds());
        } else {
            txIdsToCheck.add(normalizedId);
        }
        for (String txId : txIdsToCheck) {
            if (isRedisInventoryCompensationPending(txId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 尝试按 threadId 解锁，并将失败项持久化到 Redis；Redis 失败时写入本机回退 Map。
     *
     * @param removeWaitingAfterXa true 表示 XA 回调路径，成功后清除等待登记
     * @param redisReadFailed      true 表示 Redis 登记未成功读取，禁止 del/set Redis 补偿 key
     * @return true 表示仍有未释放锁
     */
    private static boolean attemptUnlockAndPersist(String normalizedId, RetryUnlockPayload payload,
                                                   boolean removeWaitingAfterXa, String phase,
                                                   boolean redisReadFailed) {
        Map<String, Long> lockKeyToThreadId = payload.getLockKeyToThreadId();
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
        persistFailedUnlocks(normalizedId, RetryUnlockPayload.ofLocal(failed, payload.getRedisTransactionIds()),
                redisReadFailed);
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
     * @param normalizedId    归一化全局事务 ID 或 local$ retryId
     * @param payload         锁及关联 Redis transactionId
     * @param redisReadFailed true 时仅更新本机回退，禁止覆盖 Redis 登记
     */
    private static void persistFailedUnlocks(String normalizedId, RetryUnlockPayload payload,
                                             boolean redisReadFailed) {
        if (payload.isEmpty()) {
            if (redisReadFailed) {
                clearLocalRetryStoresOnly(normalizedId);
            } else {
                clearAllRetryStores(normalizedId);
            }
            return;
        }
        if (redisReadFailed) {
            LOCAL_RETRY_UNLOCKS.put(normalizedId, payload);
            log.warn("未分配共享锁 Redis 读失败，仅回写本机补偿 transactionId={} lockCount={}",
                    normalizedId, payload.getLockKeyToThreadId().size());
            return;
        }
        if (saveRetryPayloadSafely(normalizedId, payload)) {
            LOCAL_RETRY_UNLOCKS.remove(normalizedId);
            return;
        }
        LOCAL_RETRY_UNLOCKS.put(normalizedId, payload);
        log.warn("未分配共享锁 Redis 补偿登记失败，回退本机重试 transactionId={} lockCount={}",
                normalizedId, payload.getLockKeyToThreadId().size());
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

    private static boolean saveRetryPayloadSafely(String normalizedId, RetryUnlockPayload payload) {
        try {
            saveRetryPayload(normalizedId, payload);
            return true;
        } catch (Exception e) {
            log.error("未分配共享锁 Redis 补偿登记失败 transactionId={} lockCount={}",
                    normalizedId, payload.getLockKeyToThreadId().size(), e);
            return false;
        }
    }

    /**
     * 持久化补偿登记：local$ 使用含 redisTransactionIds 的结构；全局 XID 仍用扁平 lock 映射。
     *
     * @param normalizedId 补偿登记 ID
     * @param payload      锁及关联 Redis transactionId
     */
    private static void saveRetryPayload(String normalizedId, RetryUnlockPayload payload) {
        if (payload.isEmpty()) {
            clearRetryLockMapSafely(normalizedId);
            return;
        }
        String json;
        if (isLocalRetryId(normalizedId)) {
            JSONObject root = new JSONObject();
            root.put(JSON_LOCKS, payload.getLockKeyToThreadId());
            if (CollUtil.isNotEmpty(payload.getRedisTransactionIds())) {
                root.put(JSON_REDIS_TX_IDS, payload.getRedisTransactionIds());
            }
            json = root.toJSONString();
        } else {
            json = JSON.toJSONString(payload.getLockKeyToThreadId());
        }
        resolveRedisUtil().set(buildRetryKey(normalizedId), json, RETRY_KEY_TTL_SECONDS);
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
            return RetryLockMapLoadResult.success(RetryUnlockPayload.empty());
        }
        try {
            JSONObject jsonObject = JSON.parseObject(value.toString());
            return RetryLockMapLoadResult.success(RetryUnlockPayload.parseFromJson(jsonObject));
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

    /**
     * 本机回退登记 ID（local$ 前缀）；关联 Redis transactionId 存于 {@link RetryUnlockPayload}。
     *
     * @param normalizedId 归一化事务 ID
     * @return true 表示 local$ 前缀的本机回退 ID
     */
    private static boolean isLocalRetryId(String normalizedId) {
        return StringUtils.isNotBlank(normalizedId) && normalizedId.startsWith("local$");
    }

    /**
     * 查询 Redis 库存补偿是否仍待处理；查询失败时保守推迟锁释放。
     *
     * @param transactionId 归一化全局事务 ID
     * @return true 表示仍有 Redis TRY 或补偿登记
     */
    private static boolean isRedisInventoryCompensationPending(String transactionId) {
        try {
            InventoryRedisTxCompensateRegistry registry = ApplicationContextUtils.getBean(InventoryRedisTxCompensateRegistry.class);
            return registry.isRedisInventoryCompensationPending(transactionId);
        } catch (Exception e) {
            log.warn("Job 检查 Redis 库存补偿状态失败，推迟锁释放 transactionId={}", transactionId, e);
            return true;
        }
    }

    private static String normalizeTransactionId(String transactionId) {
        return transactionId == null ? "" : transactionId.replace(":", "_");
    }

    /**
     * Job 扫描待补偿 transactionId 的收集结果，区分「无待补偿」与「Redis keys 扫描失败」。
     */
    private static final class RetryTransactionIdsResult {

        private final Set<String> transactionIds;
        private final boolean redisScanFailed;

        private RetryTransactionIdsResult(Set<String> transactionIds, boolean redisScanFailed) {
            this.transactionIds = transactionIds;
            this.redisScanFailed = redisScanFailed;
        }

        /**
         * @param transactionIds 已收集的 transactionId，允许为空
         * @return redisScanFailed=false 的收集结果
         */
        static RetryTransactionIdsResult success(Set<String> transactionIds) {
            return new RetryTransactionIdsResult(transactionIds, false);
        }

        /**
         * @param transactionIds 本机回退已收集的 transactionId，可能不含仅存在于 Redis 的登记
         * @return redisScanFailed=true，调用方不得据此认为补偿已全部完成
         */
        static RetryTransactionIdsResult scanFailed(Set<String> transactionIds) {
            return new RetryTransactionIdsResult(transactionIds, true);
        }

        Set<String> getTransactionIds() {
            return transactionIds;
        }

        boolean isRedisScanFailed() {
            return redisScanFailed;
        }
    }

    /**
     * Redis 补偿登记读取结果，区分「无登记」与「读取/解析失败」。
     */
    private static final class RetryLockMapLoadResult {

        private final RetryUnlockPayload payload;
        private final boolean readSuccess;

        private RetryLockMapLoadResult(RetryUnlockPayload payload, boolean readSuccess) {
            this.payload = payload;
            this.readSuccess = readSuccess;
        }

        /**
         * @param payload 登记内容，允许为空
         * @return readSuccess=true 的读取结果
         */
        static RetryLockMapLoadResult success(RetryUnlockPayload payload) {
            return new RetryLockMapLoadResult(payload, true);
        }

        /**
         * @return readSuccess=false，调用方不得据此清理 Redis 登记
         */
        static RetryLockMapLoadResult readFailed() {
            return new RetryLockMapLoadResult(RetryUnlockPayload.empty(), false);
        }

        RetryUnlockPayload getPayload() {
            return payload;
        }

        boolean isReadSuccess() {
            return readSuccess;
        }
    }

    /**
     * 合并多来源补偿登记的结果；redisReadFailed 为 true 且 payload 为空时禁止清理 Redis。
     */
    private static final class MergeRetryResult {

        private final RetryUnlockPayload payload;
        private final boolean redisReadFailed;

        private MergeRetryResult(RetryUnlockPayload payload, boolean redisReadFailed) {
            this.payload = payload;
            this.redisReadFailed = redisReadFailed;
        }

        RetryUnlockPayload getPayload() {
            return payload;
        }

        boolean isRedisReadFailed() {
            return redisReadFailed;
        }
    }

    /**
     * 未分配共享锁补偿载荷：lock key 映射及 local$ 路径关联的 Redis 库存 transactionId。
     */
    private static final class RetryUnlockPayload {

        private final Map<String, Long> lockKeyToThreadId;
        private final Set<String> redisTransactionIds;

        private RetryUnlockPayload(Map<String, Long> lockKeyToThreadId, Set<String> redisTransactionIds) {
            this.lockKeyToThreadId = lockKeyToThreadId;
            this.redisTransactionIds = redisTransactionIds;
        }

        /**
         * @return 空载荷
         */
        static RetryUnlockPayload empty() {
            return new RetryUnlockPayload(new LinkedHashMap<>(), new LinkedHashSet<>());
        }

        /**
         * @param locks 锁 key 与 threadId 映射
         * @return 仅含锁信息的载荷（全局 XID 路径）
         */
        static RetryUnlockPayload ofLocks(Map<String, Long> locks) {
            return new RetryUnlockPayload(new LinkedHashMap<>(locks), new LinkedHashSet<>());
        }

        /**
         * @param locks               锁 key 与 threadId 映射
         * @param redisTransactionIds 关联 Redis 库存 transactionId
         * @return local$ 本地补偿载荷
         */
        static RetryUnlockPayload ofLocal(Map<String, Long> locks, Set<String> redisTransactionIds) {
            Set<String> normalizedTxIds = new LinkedHashSet<>();
            if (redisTransactionIds != null) {
                for (String txId : redisTransactionIds) {
                    if (StringUtils.isNotBlank(txId)) {
                        normalizedTxIds.add(normalizeTransactionId(txId));
                    }
                }
            }
            return new RetryUnlockPayload(new LinkedHashMap<>(locks), normalizedTxIds);
        }

        /**
         * 自 Redis JSON 解析；兼容扁平 lock 映射与 local$ 扩展结构。
         *
         * @param jsonObject Redis 登记 JSON
         * @return 补偿载荷
         */
        static RetryUnlockPayload parseFromJson(JSONObject jsonObject) {
            if (jsonObject.containsKey(JSON_LOCKS)) {
                Map<String, Long> locks = parseLockMap(jsonObject.getJSONObject(JSON_LOCKS));
                Set<String> txIds = new LinkedHashSet<>();
                if (jsonObject.containsKey(JSON_REDIS_TX_IDS)) {
                    jsonObject.getJSONArray(JSON_REDIS_TX_IDS).forEach(item -> {
                        if (item != null && StringUtils.isNotBlank(item.toString())) {
                            txIds.add(normalizeTransactionId(item.toString()));
                        }
                    });
                }
                return new RetryUnlockPayload(locks, txIds);
            }
            return RetryUnlockPayload.ofLocks(parseLockMap(jsonObject));
        }

        /**
         * @param jsonObject lock key → threadId JSON 对象
         * @return 锁映射
         */
        private static Map<String, Long> parseLockMap(JSONObject jsonObject) {
            Map<String, Long> lockKeyToThreadId = new LinkedHashMap<>();
            if (jsonObject == null) {
                return lockKeyToThreadId;
            }
            for (String lockKey : jsonObject.keySet()) {
                lockKeyToThreadId.put(lockKey, jsonObject.getLongValue(lockKey));
            }
            return lockKeyToThreadId;
        }

        /**
         * 合并锁映射与 Redis transactionId 集合。
         *
         * @param other 另一载荷
         * @return 合并结果
         */
        RetryUnlockPayload merge(RetryUnlockPayload other) {
            Map<String, Long> locks = new LinkedHashMap<>(this.lockKeyToThreadId);
            other.lockKeyToThreadId.forEach(locks::putIfAbsent);
            Set<String> txIds = new LinkedHashSet<>(this.redisTransactionIds);
            txIds.addAll(other.redisTransactionIds);
            return new RetryUnlockPayload(locks, txIds);
        }

        Map<String, Long> getLockKeyToThreadId() {
            return lockKeyToThreadId;
        }

        Set<String> getRedisTransactionIds() {
            return redisTransactionIds;
        }

        boolean isEmpty() {
            return lockKeyToThreadId.isEmpty();
        }
    }
}
