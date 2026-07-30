package com.erp.server.wms.inventory.tx.compensate;

import com.erp.server.wms.inventory.tx.support.SeataGlobalTxStatusHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.ApplicationContextUtils;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 库存事务补偿：孤儿 TRY 自动 rollback、DB 已提交但 Redis commit 失败自动重试 commit。
 */
@Slf4j
public final class InventoryRedisTxCompensateHelper {

    /** 默认 commit 重试等待秒数（回调失败场景） */
    public static final int DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS = 60;

    /** 默认孤儿 TRY 回滚等待秒数 */
    public static final int DEFAULT_ORPHAN_ROLLBACK_TIMEOUT_SECONDS = 1800;

    /** DB transactionId 批量查询上限 */
    private static final int DB_QUERY_BATCH_SIZE = 500;

    private InventoryRedisTxCompensateHelper() {
    }

    /**
     * 补偿执行结果，供 Job 判断是否需要返回失败。
     */
    @Getter
    public static class CompensateResult {
        private int commitRetryFailureCount;
        private int orphanRollbackFailureCount;
        private int dbQueryFailureCount;

        /**
         * @return 全部失败项合计
         */
        public int getTotalFailureCount() {
            return commitRetryFailureCount + orphanRollbackFailureCount + dbQueryFailureCount;
        }

        void incrementCommitRetryFailure() {
            commitRetryFailureCount++;
        }

        void incrementOrphanRollbackFailure() {
            orphanRollbackFailureCount++;
        }

        void addDbQueryFailureCount(int count) {
            dbQueryFailureCount += count;
        }
    }

    /**
     * DB 分批查询结果：成功命中的 transactionId 与查询失败待下轮重试的 transactionId。
     */
    private static final class DbBatchLoadResult {
        private final Set<String> dbTransactionIds = new HashSet<>();
        private final Set<String> queryFailedTransactionIds = new HashSet<>();

        private Set<String> getDbTransactionIds() {
            return dbTransactionIds;
        }

        private Set<String> getQueryFailedTransactionIds() {
            return queryFailedTransactionIds;
        }
    }

    /**
     * 解析并校验 Job 补偿超时参数；非法值回退默认并打 warn 日志。
     *
     * @param jobParam XXL-JOB 入参 JSON
     * @return [orphanRollbackTimeoutSeconds, commitRetryTimeoutSeconds]
     */
    public static int[] resolveCompensateTimeouts(String jobParam) {
        int orphanRollbackTimeout = DEFAULT_ORPHAN_ROLLBACK_TIMEOUT_SECONDS;
        int commitRetryTimeout = DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS;
        if (StringUtils.isNotBlank(jobParam)) {
            try {
                JSONObject parseObject = JSON.parseObject(jobParam);
                if (parseObject.containsKey("timeout")) {
                    orphanRollbackTimeout = parseObject.getIntValue("timeout");
                }
                if (parseObject.containsKey("orphanRollbackTimeout")) {
                    orphanRollbackTimeout = parseObject.getIntValue("orphanRollbackTimeout");
                }
                if (parseObject.containsKey("commitRetryTimeout")) {
                    commitRetryTimeout = parseObject.getIntValue("commitRetryTimeout");
                }
            } catch (Exception e) {
                log.warn("inventoryCheckRollback 参数解析失败 jobParam={}", jobParam, e);
            }
        }
        return new int[] {
                normalizeOrphanRollbackTimeout(orphanRollbackTimeout),
                normalizeCommitRetryTimeout(commitRetryTimeout)
        };
    }

    /**
     * 校验孤儿 TRY 回滚等待秒数，非法值回退默认。
     *
     * @param timeoutSeconds Job 配置的等待秒数
     * @return 合法等待秒数
     */
    public static int normalizeOrphanRollbackTimeout(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            log.warn("orphanRollbackTimeout 非法 value={}，回退默认 {}", timeoutSeconds,
                    DEFAULT_ORPHAN_ROLLBACK_TIMEOUT_SECONDS);
            return DEFAULT_ORPHAN_ROLLBACK_TIMEOUT_SECONDS;
        }
        return timeoutSeconds;
    }

    /**
     * 校验 commit 重试等待秒数，非法值回退默认。
     *
     * @param timeoutSeconds Job 配置的等待秒数
     * @return 合法等待秒数
     */
    public static int normalizeCommitRetryTimeout(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            log.warn("commitRetryTimeout 非法 value={}，回退默认 {}", timeoutSeconds,
                    DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS);
            return DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS;
        }
        return timeoutSeconds;
    }

    /**
     * 扫描 Redis 残留 TRANSACTION，按 DB 是否存在 transaction 记录分流补偿。
     *
     * @param redisTransactionKeys         Redis TRANSACTION key 集合
     * @param loadDbTransactionIds         根据 Redis transactionId 查询 DB 中仍存在的 transactionId
     * @param commitRedis                  commit 回调（Job 补偿应传 {@code toDoHis=false}）
     * @param rollbackRedis                rollback 回调
     * @param kind                         实体仓或虚拟仓
     * @param orphanRollbackTimeoutSeconds 孤儿 TRY 首次发现后等待秒数
     * @param commitRetryTimeoutSeconds    DB 已提交但 Redis commit 失败的重试等待秒数
     * @return 补偿执行结果（含失败计数）
     */
    public static CompensateResult compensate(Collection<String> redisTransactionKeys,
                                              Function<Set<String>, Set<String>> loadDbTransactionIds,
                                              Consumer<String> commitRedis,
                                              Consumer<String> rollbackRedis,
                                              InventoryRedisTxCompensateRegistry.TxKind kind,
                                              int orphanRollbackTimeoutSeconds,
                                              int commitRetryTimeoutSeconds) {
        CompensateResult result = new CompensateResult();
        InventoryRedisTxCompensateRegistry registry = ApplicationContextUtils.getBean(InventoryRedisTxCompensateRegistry.class);
        Set<String> pendingTransactions = CollUtil.isEmpty(redisTransactionKeys)
                ? new HashSet<>()
                : redisTransactionKeys.stream()
                        .map(InventoryRedisTxCompensateHelper::extractTransactionId)
                        .collect(Collectors.toSet());
        registry.pruneStale(kind, pendingTransactions);

        Set<String> registeredCommitRetryIds = registry.listPendingCommitRetryTransactionIds(kind);
        if (CollUtil.isEmpty(pendingTransactions) && CollUtil.isEmpty(registeredCommitRetryIds)) {
            return result;
        }

        Set<String> commitRetryScanIds = new HashSet<>(pendingTransactions);
        commitRetryScanIds.addAll(registeredCommitRetryIds);

        Set<String> idsToQueryDb = new HashSet<>(pendingTransactions);
        idsToQueryDb.addAll(registeredCommitRetryIds);
        DbBatchLoadResult dbBatchLoadResult = loadDbTransactionIdsInBatches(idsToQueryDb, loadDbTransactionIds, result);
        Set<String> dbTransactionIds = dbBatchLoadResult.getDbTransactionIds();
        Set<String> dbQueryFailedIds = dbBatchLoadResult.getQueryFailedTransactionIds();

        compensateCommitRetry(registry, commitRetryScanIds, dbTransactionIds, dbQueryFailedIds, commitRedis, kind,
                commitRetryTimeoutSeconds, result);
        compensateOrphanRollback(registry, pendingTransactions, dbTransactionIds, dbQueryFailedIds, rollbackRedis, kind,
                orphanRollbackTimeoutSeconds, result);
        if (result.getTotalFailureCount() > 0) {
            log.warn("inventoryCheckRollback 补偿存在失败 kind={} commitRetryFailures={} orphanRollbackFailures={} dbQueryFailures={}",
                    kind, result.getCommitRetryFailureCount(), result.getOrphanRollbackFailureCount(),
                    result.getDbQueryFailureCount());
        }
        return result;
    }

    /**
     * 从 Redis TRANSACTION key 解析 transactionId（取最后一段）。
     *
     * @param redisKey Redis key
     * @return transactionId
     */
    public static String extractTransactionId(String redisKey) {
        String[] split = redisKey.split(":");
        return split[split.length - 1];
    }

    /**
     * 分批查询 DB 中存在的 transactionId，单批失败不阻断其余批次。
     *
     * @param transactionIds       待查询 transactionId
     * @param loadDbTransactionIds   单批 DB 查询
     * @param result                 失败计数写入对象
     * @return 成功命中与查询失败的 transactionId 分流结果
     */
    private static DbBatchLoadResult loadDbTransactionIdsInBatches(Set<String> transactionIds,
                                                                   Function<Set<String>, Set<String>> loadDbTransactionIds,
                                                                   CompensateResult result) {
        DbBatchLoadResult batchLoadResult = new DbBatchLoadResult();
        if (CollUtil.isEmpty(transactionIds)) {
            return batchLoadResult;
        }
        List<String> idList = new ArrayList<>(transactionIds);
        for (int i = 0; i < idList.size(); i += DB_QUERY_BATCH_SIZE) {
            int end = Math.min(i + DB_QUERY_BATCH_SIZE, idList.size());
            Set<String> batch = new HashSet<>(idList.subList(i, end));
            try {
                Set<String> batchResult = loadDbTransactionIds.apply(batch);
                if (CollUtil.isNotEmpty(batchResult)) {
                    batchLoadResult.getDbTransactionIds().addAll(batchResult);
                }
            } catch (Exception e) {
                batchLoadResult.getQueryFailedTransactionIds().addAll(batch);
                result.addDbQueryFailureCount(batch.size());
                log.error("inventoryCheckRollback DB 分批查询失败 batchSize={} fromIndex={}", batch.size(), i, e);
            }
        }
        if (CollUtil.isNotEmpty(batchLoadResult.getQueryFailedTransactionIds())) {
            log.warn("inventoryCheckRollback DB 分批查询跳过补偿 transactionCount={}",
                    batchLoadResult.getQueryFailedTransactionIds().size());
        }
        return batchLoadResult;
    }

    /**
     * DB 已有 transaction 记录、Redis 仍残留 TRY 时，超时后重试 commit。
     */
    private static void compensateCommitRetry(InventoryRedisTxCompensateRegistry registry,
                                              Set<String> commitRetryScanIds,
                                              Set<String> dbTransactionIds,
                                              Set<String> dbQueryFailedIds,
                                              Consumer<String> commitRedis,
                                              InventoryRedisTxCompensateRegistry.TxKind kind,
                                              int commitRetryTimeoutSeconds,
                                              CompensateResult result) {
        if (CollUtil.isEmpty(commitRetryScanIds)) {
            return;
        }
        commitRetryScanIds.stream()
                .filter(transactionId -> !dbTransactionIds.contains(transactionId))
                .filter(transactionId -> !dbQueryFailedIds.contains(transactionId))
                .forEach(transactionId -> {
                    try {
                        log.warn("inventoryCheckCommitRetry 清理无效登记 kind={} transactionId={} reason=dbRecordMissing",
                                kind, transactionId);
                        registry.clearPending(kind, transactionId, false);
                    } catch (Exception e) {
                        result.incrementCommitRetryFailure();
                        log.error("inventoryCheckCommitRetry 清理无效登记失败 kind={} transactionId={}", kind, transactionId, e);
                    }
                });

        Set<String> commitRetryCandidates = commitRetryScanIds.stream()
                .filter(dbTransactionIds::contains)
                .filter(transactionId -> !dbQueryFailedIds.contains(transactionId))
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(commitRetryCandidates)) {
            return;
        }
        commitRetryCandidates.forEach(transactionId -> {
            try {
                registry.touchPending(kind, transactionId, false, commitRetryTimeoutSeconds);
                if (!registry.isDue(kind, transactionId, commitRetryTimeoutSeconds, false)) {
                    return;
                }
                if (!registry.hasRedisTransactionKey(kind, transactionId)) {
                    log.warn("inventoryCheckCommitRetry 清理 commit 补偿登记 kind={} transactionId={} reason=transactionKeyMissing",
                            kind, transactionId);
                    registry.clearPending(kind, transactionId, false);
                    return;
                }
                log.warn("inventoryCheckCommitRetry 自动补偿提交开始 kind={} transactionId={}", kind, transactionId);
                commitRedis.accept(transactionId);
                registry.clearPending(kind, transactionId, false);
                log.warn("inventoryCheckCommitRetry 自动补偿提交结束 kind={} transactionId={}", kind, transactionId);
            } catch (Exception e) {
                result.incrementCommitRetryFailure();
                log.error("inventoryCheckCommitRetry 自动补偿提交失败 kind={} transactionId={}", kind, transactionId, e);
            }
        });
    }

    /**
     * Redis 残留 TRY 但 DB 无 transaction 记录时，超时后 rollback 清理孤儿预占。
     */
    private static void compensateOrphanRollback(InventoryRedisTxCompensateRegistry registry,
                                                 Set<String> pendingTransactions,
                                                 Set<String> dbTransactionIds,
                                                 Set<String> dbQueryFailedIds,
                                                 Consumer<String> rollbackRedis,
                                                 InventoryRedisTxCompensateRegistry.TxKind kind,
                                                 int orphanRollbackTimeoutSeconds,
                                                 CompensateResult result) {
        Set<String> orphanTransactions = new HashSet<>(pendingTransactions);
        orphanTransactions.removeAll(dbTransactionIds);
        orphanTransactions.removeAll(dbQueryFailedIds);
        if (CollUtil.isEmpty(orphanTransactions)) {
            return;
        }
        orphanTransactions.forEach(transactionId -> {
            try {
                if (SeataGlobalTxStatusHelper.shouldDeferOrphanRollback(transactionId)) {
                    return;
                }
                registry.touchPending(kind, transactionId, true, orphanRollbackTimeoutSeconds);
                if (!registry.isDue(kind, transactionId, orphanRollbackTimeoutSeconds, true)) {
                    return;
                }
                log.warn("inventoryCheckRollback 自动回滚开始 kind={} transactionId={}", kind, transactionId);
                rollbackRedis.accept(transactionId);
                registry.clearPending(kind, transactionId, true);
                log.warn("inventoryCheckRollback 自动回滚结束 kind={} transactionId={}", kind, transactionId);
            } catch (Exception e) {
                result.incrementOrphanRollbackFailure();
                log.error("inventoryCheckRollback 自动回滚失败 kind={} transactionId={}", kind, transactionId, e);
            }
        });
    }
}
