package com.erp.server.wms.inventory.tx.support;

import org.apache.commons.lang3.StringUtils;

import io.seata.core.exception.TransactionException;
import io.seata.core.model.GlobalStatus;
import io.seata.tm.TransactionManagerHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Seata 全局事务状态查询，供孤儿 TRY 补偿判断全局事务是否仍在进行中。
 */
@Slf4j
public final class SeataGlobalTxStatusHelper {

    private SeataGlobalTxStatusHelper() {
    }

    /**
     * 是否应推迟孤儿 TRY rollback（全局事务可能仍在进行或状态未知）。
     *
     * @param normalizedTransactionId Redis 事务 ID（全局 XID 已将 {@code :} 替换为 {@code _}）
     * @return true 表示不应按 DB 缺失直接 rollback Redis TRY
     */
    public static boolean shouldDeferOrphanRollback(String normalizedTransactionId) {
        if (StringUtils.isBlank(normalizedTransactionId) || !looksLikeGlobalTransactionId(normalizedTransactionId)) {
            return false;
        }
        String xid = denormalizeXid(normalizedTransactionId);
        try {
            GlobalStatus status = TransactionManagerHolder.get().getStatus(xid);
            if (status == null) {
                log.warn("Seata 全局事务状态为空，跳过孤儿 rollback xid={}", xid);
                return true;
            }
            if (isPossiblyActiveGlobalStatus(status)) {
                log.warn("Seata 全局事务仍进行中，跳过孤儿 rollback xid={} status={}", xid, status);
                return true;
            }
            return false;
        } catch (TransactionException e) {
            log.warn("Seata 全局事务状态查询失败，跳过孤儿 rollback xid={}", xid, e);
            return true;
        } catch (Exception e) {
            log.warn("Seata 全局事务状态查询异常，跳过孤儿 rollback xid={}", xid, e);
            return true;
        }
    }

    /**
     * 归一化 XID 还原为 Seata 原始格式（{@code _} → {@code :}）。
     *
     * @param normalizedTransactionId 下划线形式 transactionId
     * @return Seata XID
     */
    static String denormalizeXid(String normalizedTransactionId) {
        return normalizedTransactionId.replace("_", ":");
    }

    /**
     * 判断是否可能为 Seata 全局 XID（本地 traceId/flowId 通常不含 {@code _}）。
     *
     * @param normalizedTransactionId Redis 事务 ID
     * @return true 表示可能为全局 XID
     */
    static boolean looksLikeGlobalTransactionId(String normalizedTransactionId) {
        return normalizedTransactionId.indexOf('_') >= 0;
    }

    /**
     * @param status Seata 全局事务状态
     * @return true 表示事务未明确终止，不宜 rollback Redis TRY
     */
    private static boolean isPossiblyActiveGlobalStatus(GlobalStatus status) {
        switch (status) {
            case Begin:
            case Committing:
            case CommitRetrying:
            case Rollbacking:
            case RollbackRetrying:
            case TimeoutRollbacking:
            case UnKnown:
                return true;
            default:
                return false;
        }
    }
}
