package com.erp.server.wms.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.business.utils.ApplicationContextUtils;

import cn.hutool.core.collection.CollUtil;
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

    private InventoryRedisTxCompensateHelper() {
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
     */
    public static void compensate(Collection<String> redisTransactionKeys,
                                  Function<Set<String>, Set<String>> loadDbTransactionIds,
                                  Consumer<String> commitRedis,
                                  Consumer<String> rollbackRedis,
                                  InventoryRedisTxCompensateRegistry.TxKind kind,
                                  int orphanRollbackTimeoutSeconds,
                                  int commitRetryTimeoutSeconds) {
        InventoryRedisTxCompensateRegistry registry = ApplicationContextUtils.getBean(InventoryRedisTxCompensateRegistry.class);
        Set<String> pendingTransactions = CollUtil.isEmpty(redisTransactionKeys)
                ? new HashSet<>()
                : redisTransactionKeys.stream()
                        .map(InventoryRedisTxCompensateHelper::extractTransactionId)
                        .collect(Collectors.toSet());
        registry.pruneStale(kind, pendingTransactions);

        Set<String> registeredCommitRetryIds = registry.listPendingCommitRetryTransactionIds(kind);
        if (CollUtil.isEmpty(pendingTransactions) && CollUtil.isEmpty(registeredCommitRetryIds)) {
            return;
        }

        Set<String> commitRetryScanIds = new HashSet<>(pendingTransactions);
        commitRetryScanIds.addAll(registeredCommitRetryIds);

        Set<String> idsToQueryDb = new HashSet<>(pendingTransactions);
        idsToQueryDb.addAll(registeredCommitRetryIds);
        Set<String> dbTransactionIds = CollUtil.isEmpty(idsToQueryDb)
                ? Collections.emptySet()
                : loadDbTransactionIds.apply(idsToQueryDb);

        compensateCommitRetry(registry, commitRetryScanIds, dbTransactionIds, commitRedis, kind, commitRetryTimeoutSeconds);
        compensateOrphanRollback(registry, pendingTransactions, dbTransactionIds, rollbackRedis, kind, orphanRollbackTimeoutSeconds);
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
     * DB 已有 transaction 记录、Redis 仍残留 TRY 时，超时后重试 commit。
     *
     * @param registry                  补偿登记
     * @param commitRetryScanIds        TRANSACTION key 与 commit 补偿登记合并后的 transactionId
     * @param dbTransactionIds          DB 中存在的 transactionId
     * @param commitRedis               commit 回调
     * @param kind                      实体仓或虚拟仓
     * @param commitRetryTimeoutSeconds 重试等待秒数
     */
    private static void compensateCommitRetry(InventoryRedisTxCompensateRegistry registry,
                                            Set<String> commitRetryScanIds,
                                            Set<String> dbTransactionIds,
                                            Consumer<String> commitRedis,
                                            InventoryRedisTxCompensateRegistry.TxKind kind,
                                            int commitRetryTimeoutSeconds) {
        if (CollUtil.isEmpty(commitRetryScanIds)) {
            return;
        }
        commitRetryScanIds.stream()
                .filter(transactionId -> !dbTransactionIds.contains(transactionId))
                .forEach(transactionId -> {
                    log.warn("inventoryCheckCommitRetry 清理无效登记 kind={} transactionId={} reason=dbRecordMissing",
                            kind, transactionId);
                    registry.clearPending(kind, transactionId, false);
                });

        Set<String> commitRetryCandidates = commitRetryScanIds.stream()
                .filter(dbTransactionIds::contains)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(commitRetryCandidates)) {
            return;
        }
        commitRetryCandidates.forEach(transactionId -> {
            try {
                registry.touchPending(kind, transactionId, false);
                if (!registry.isDue(kind, transactionId, commitRetryTimeoutSeconds, false)) {
                    return;
                }
                if (!registry.hasRedisTransactionKey(kind, transactionId)) {
                    log.warn("inventoryCheckCommitRetry 跳过补偿提交 kind={} transactionId={} reason=transactionKeyMissing",
                            kind, transactionId);
                    return;
                }
                log.warn("inventoryCheckCommitRetry 自动补偿提交开始 kind={} transactionId={}", kind, transactionId);
                commitRedis.accept(transactionId);
                registry.clearPending(kind, transactionId, false);
                log.warn("inventoryCheckCommitRetry 自动补偿提交结束 kind={} transactionId={}", kind, transactionId);
            } catch (Exception e) {
                log.error("inventoryCheckCommitRetry 自动补偿提交失败 kind={} transactionId={}", kind, transactionId, e);
            }
        });
    }

    /**
     * Redis 残留 TRY 但 DB 无 transaction 记录时，超时后 rollback 清理孤儿预占。
     *
     * @param registry                     补偿登记
     * @param pendingTransactions          Redis 残留 transactionId
     * @param dbTransactionIds             DB 中存在的 transactionId
     * @param rollbackRedis                rollback 回调
     * @param kind                         实体仓或虚拟仓
     * @param orphanRollbackTimeoutSeconds 回滚等待秒数
     */
    private static void compensateOrphanRollback(InventoryRedisTxCompensateRegistry registry,
                                                 Set<String> pendingTransactions,
                                                 Set<String> dbTransactionIds,
                                                 Consumer<String> rollbackRedis,
                                                 InventoryRedisTxCompensateRegistry.TxKind kind,
                                                 int orphanRollbackTimeoutSeconds) {
        Set<String> orphanTransactions = new HashSet<>(pendingTransactions);
        orphanTransactions.removeAll(dbTransactionIds);
        if (CollUtil.isEmpty(orphanTransactions)) {
            return;
        }
        orphanTransactions.forEach(transactionId -> {
            registry.touchPending(kind, transactionId, true);
            if (!registry.isDue(kind, transactionId, orphanRollbackTimeoutSeconds, true)) {
                return;
            }
            try {
                log.warn("inventoryCheckRollback 自动回滚开始 kind={} transactionId={}", kind, transactionId);
                rollbackRedis.accept(transactionId);
                registry.clearPending(kind, transactionId, true);
                log.warn("inventoryCheckRollback 自动回滚结束 kind={} transactionId={}", kind, transactionId);
            } catch (Exception e) {
                log.error("inventoryCheckRollback 自动回滚失败 kind={} transactionId={}", kind, transactionId, e);
            }
        });
    }
}
