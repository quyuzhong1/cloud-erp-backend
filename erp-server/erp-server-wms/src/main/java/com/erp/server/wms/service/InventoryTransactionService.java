package com.erp.server.wms.service;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.common.business.service.SuperService;

import java.time.LocalDate;
import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.wms.dto.InventoryTransactionDTO;

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
    * 新增
    * @author shukai
    * @date: 2025-10-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InventoryTransactionDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-10-13
    * @param dto
    * @return
    */
    Boolean update(InventoryTransactionDTO.UpdateDTO dto);

    
    /**
     * 最新历史库存同步即时库存
     * @param inventoryId
     */
    void inventoryHisToInventory(String inventoryId);
    
    
   
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
