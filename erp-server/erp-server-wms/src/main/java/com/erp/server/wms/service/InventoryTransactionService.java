package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.InventoryTransactionEntity;

/**
 * <p>
 * 库存事务表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
 */
public interface InventoryTransactionService extends SuperService<InventoryTransactionEntity> {

    /**
     * 库存交易同步历史库存，调用inventoryHisToInventory同步即时库存
     * @param inventoryId
     * @param size
     */
    void inventoryTransactionToInventoryHis(String inventoryId , int size);
    
    /**
     * 提交redis库存
     * @param transactionId
     */
    void commitRedis(String transactionId);
    
    /**
     * 回滚redis库存
     * @param transactionId
     */
    void rollbackRedis(String transactionId);
}
