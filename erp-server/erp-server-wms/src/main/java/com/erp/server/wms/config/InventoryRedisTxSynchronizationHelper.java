package com.erp.server.wms.config;

import org.springframework.transaction.support.TransactionSynchronization;

import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.wms.service.InventoryTransactionService;
import com.erp.server.wms.service.VirtualInventoryTransactionService;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 库存 Redis 事务同步回调：本地/Spring 事务与 Seata XA 回调统一在此 commit/rollback Redis TRY。
 * 失败时打 error 日志并发送系统异常预警，避免静默跳过导致 Redis 与 DB 长期不一致。
 */
@Slf4j
public final class InventoryRedisTxSynchronizationHelper {

    private InventoryRedisTxSynchronizationHelper() {
    }

    /**
     * 实体仓库存 Redis 事务完成回调。
     *
     * @param transactionId Redis 事务 ID（全局事务下为 Seata XID）
     * @param status          {@link TransactionSynchronization#STATUS_COMMITTED} 或 {@link TransactionSynchronization#STATUS_ROLLED_BACK}
     * @param needCommit      为 false 时 commit 回调跳过（分布式事务等待 XA 最终提交）
     */
    public static void afterEntityCompletion(String transactionId, int status, boolean needCommit) {
        String operation = resolveOperation(status, needCommit);
        if (operation == null) {
            return;
        }
        try {
            InventoryTransactionService service = ApplicationContextUtils.getBean(InventoryTransactionService.class);
            if (TransactionSynchronization.STATUS_COMMITTED == status) {
                service.commitRedis(transactionId, true);
            } else {
                service.rollbackRedis(transactionId);
            }
        } catch (Exception e) {
            log.error("实体仓库存事务回调 Redis {} 失败 transactionId={}", operation, transactionId, e);
            if ("commit".equals(operation)) {
                try {
                    ApplicationContextUtils.getBean(InventoryRedisTxCompensateRegistry.class)
                            .markCommitRetryFailed(InventoryRedisTxCompensateRegistry.TxKind.ENTITY, transactionId);
                } catch (Exception registryEx) {
                    log.error("实体仓 commit 补偿登记失败 transactionId={}", transactionId, registryEx);
                }
            }
            sendCallbackFailureWarn(transactionId, operation, "inventory_transaction", e);
        }
    }

    /**
     * 虚拟仓库存 Redis 事务完成回调。
     *
     * @param transactionId Redis 事务 ID
     * @param status          事务完成状态
     * @param needCommit      为 false 时 commit 回调跳过
     */
    public static void afterVirtualCompletion(String transactionId, int status, boolean needCommit) {
        String operation = resolveOperation(status, needCommit);
        if (operation == null) {
            return;
        }
        try {
            VirtualInventoryTransactionService service = ApplicationContextUtils.getBean(VirtualInventoryTransactionService.class);
            if (TransactionSynchronization.STATUS_COMMITTED == status) {
                service.commitRedis(transactionId, true);
            } else {
                service.rollbackRedis(transactionId);
            }
        } catch (Exception e) {
            log.error("虚拟仓库存事务回调 Redis {} 失败 transactionId={}", operation, transactionId, e);
            if ("commit".equals(operation)) {
                try {
                    ApplicationContextUtils.getBean(InventoryRedisTxCompensateRegistry.class)
                            .markCommitRetryFailed(InventoryRedisTxCompensateRegistry.TxKind.VIRTUAL, transactionId);
                } catch (Exception registryEx) {
                    log.error("虚拟仓 commit 补偿登记失败 transactionId={}", transactionId, registryEx);
                }
            }
            sendCallbackFailureWarn(transactionId, operation, "virtual_inventory_transaction", e);
        }
    }

    /**
     * 解析本次回调应对 Redis 执行的操作；commit 且 needCommit=false 时返回 null 表示跳过。
     *
     * @param status     事务完成状态
     * @param needCommit 是否在本回调执行 commit
     * @return commit / rollback；无需操作时返回 null
     */
    private static String resolveOperation(int status, boolean needCommit) {
        if (TransactionSynchronization.STATUS_COMMITTED == status) {
            return needCommit ? "commit" : null;
        }
        return "rollback";
    }

    /**
     * Redis 事务回调失败时发送系统异常预警（MQ），便于运维补偿残留 TRY。
     *
     * @param transactionId Redis 事务 ID
     * @param operation     commit 或 rollback
     * @param tableName     关联表名（审计用）
     * @param cause         失败原因
     */
    private static void sendCallbackFailureWarn(String transactionId, String operation, String tableName, Exception cause) {
        try {
            MQProducerService mqProducerService = ApplicationContextUtils.getBean(MQProducerService.class);
            WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
            warnMsgInfo.setBizName("预警消息");
            warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
            warnMsgInfo.setTitle("库存Redis事务回调失败");
            warnMsgInfo.setTableName(tableName);
            warnMsgInfo.setTableId(transactionId);
            String errMsg = cause.getMessage();
            if (CharSequenceUtil.isNotBlank(errMsg) && errMsg.length() > 500) {
                errMsg = errMsg.substring(0, 500);
            }
            warnMsgInfo.setKeyInfo(CharSequenceUtil.format("transactionId={}, operation={}, error={}",
                    transactionId, operation, errMsg));
            warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
            mqProducerService.sendWarnMsg(warnMsgInfo);
        } catch (Exception warnEx) {
            log.warn("发送库存Redis事务回调失败预警异常 transactionId={} operation={}", transactionId, operation, warnEx);
        }
    }
}
