package com.erp.server.wms.service;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.erp.server.wms.config.InventoryRedisTxCompensateHelper;

import cn.hutool.core.lang.Pair;

/**
 * <p>
 * 库存事务表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
 */
public interface InventoryTransactionService extends SuperService<InventoryTransactionEntity> {

	Map<String , Boolean> overrideDbInventory(LocalDate startDate , List<String> inventoryIds);
	
	Pair<String, Boolean> overrideDb(LocalDate startDate , String inventoryId);
	
	Map<String , Boolean> overrideRedisInventory(List<String> inventoryIds , boolean isCheck);
	
	/**
     * 库存id，库存交易同步历史库存，调用inventoryHisToInventory同步即时库存
     * @param inventoryId
     * @param size
     */
    void inventoryIdToInventoryHis(String inventoryId , String transactionId);
    
    /**
     * 仅内部调用
     * 仅内部调用
     * 仅内部调用
     * 库存id，库存交易同步历史库存，调用inventoryHisToInventory同步即时库存
     * @param inventoryId
     * @param size
     */
    void innerInventoryIdToInventoryHis(String inventoryId ,  String transactionId);
    
    /**
     * 事务id，库存交易同步历史库存，调用inventoryHisToInventory同步即时库存
     * @param inventoryId
     */
    void transactionIdToInventoryHis(String transactionId);
    
    /**
     * 新增库存交易
     * @param transactionList
     * @param approveType
     */
    void addInventoryTransaction(List<InventoryTransactionDTO> transactionList, String approveType);
    
    /**
     * 冻结redis库存
     * @param transactionId
     * @param transactionList
     */
    void tryRedis(String transactionId , List<InventoryTransactionDTO> transactionList);
    
    /**
     * 提交redis库存
     * @param transactionId
     */
    void commitRedis(String transactionId , boolean toDoHis);
    
    /**
     * 回滚redis库存
     * @param transactionId
     */
    void rollbackRedis(String transactionId);
    
    /**
     * 检查 Redis 残留 TRY：孤儿 rollback + DB 已提交但 commit 失败的重试。
     *
     * @param orphanRollbackTimeoutSeconds 孤儿 TRY 回滚等待秒数
     * @param commitRetryTimeoutSeconds    commit 重试等待秒数
     * @return 补偿失败项合计，0 表示全部成功或无待补偿项
     */
    int inventoryCheckRollback(int orphanRollbackTimeoutSeconds, int commitRetryTimeoutSeconds);

    /**
     * 兼容旧 Job 参数：commit 重试默认 {@link com.erp.server.wms.config.InventoryRedisTxCompensateHelper#DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS}s。
     *
     * @param timeout 孤儿 TRY 回滚等待秒数
     * @return 补偿失败项合计
     */
    default int inventoryCheckRollback(int timeout) {
        return inventoryCheckRollback(timeout, InventoryRedisTxCompensateHelper.DEFAULT_COMMIT_RETRY_TIMEOUT_SECONDS);
    }
    
    /**
     * 检查库存交易一致性
     */
    void queryInventoryCheckSame(Integer warnSize);
    
    /**
     * 查询仓+SKU 未分配在途预占量（TRY 阶段写入 reserve key）。
     *
     * @param warehouseId           仓库 ID
     * @param skuId                 SKU ID
     * @param excludeTransactionId  排除的事务 ID，可为空
     * @param excludeOperationId    排除的操作 ID，可为空；两者均非空时才排除对应片段
     * @return 在途预占出库量之和
     */
    int getUnallocPendingReserveQty(String warehouseId, String skuId, String excludeTransactionId, String excludeOperationId);

    /**
     * 解析 Redis TRY/commit/rollback 使用的事务 ID，规则与 {@link #addInventoryTransaction} 一致。
     * <p>Seata 全局事务取 XID；否则取 TraceId；无效时回退 {@code fallbackFlowId}（通常为库存流水 ID）。</p>
     *
     * @param fallbackFlowId 本地 Trace 不可用时的回退流水 ID，可为空
     * @return 事务 ID，可能为空字符串
     */
    String resolveRedisTransactionId(String fallbackFlowId);

    /**
     * 从已排序交易列表解析 Redis 事务 ID（预检与 tryRedis 共用，回退 id 取列表首条流水）。
     *
     * @param transactionList 库存交易列表（调用方应已排序）
     * @return 事务 ID
     */
    String resolveRedisTransactionIdFromList(List<InventoryTransactionDTO> transactionList);

    /**
     * 根据库存id获取redis可用库存
     * @param inventoryId
     * @return
     */
    Integer getRedisQtyByInventory(String inventoryId);

    /**
     * 读取 Redis {@code inventory:current} 基量（首段），不含 TRY 在途段；供未分配校验汇总实体仓数量。
     *
     * @param inventoryId 库存 ID
     * @return 基量，key 不存在时返回 0
     */
    Integer getRedisBaseQtyByInventory(String inventoryId);

    /**
     * 批量读取 Redis {@code inventory:current} 基量（MGET），供未分配预检一次性加载。
     *
     * @param inventoryIds 库存 ID 集合
     * @return inventoryId → 基量
     */
    Map<String, Integer> getRedisBaseQtyByInventoryBatch(Collection<String> inventoryIds);
}
