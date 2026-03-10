package com.erp.server.wms.service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;

import cn.hutool.core.lang.Pair;

/**
 * <p>
 * 虚拟仓库存事务表 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-12-18
 */
public interface VirtualInventoryTransactionService extends SuperService<VirtualInventoryTransactionEntity> {

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
    void addInventoryTransaction(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList, String approveType);
    
    /**
     * 冻结redis库存
     * @param transactionId
     * @param transactionList
     */
    void tryRedis(String transactionId , List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList);
    
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
     * 检查库存是否长时间未回滚
     */
    void inventoryCheckRollback(int timeout);
    
    /**
     * 检查库存交易一致性
     */
    void queryInventoryCheckSame(Integer warnSize);
    
    /**
     * 根据库存id获取redis可用库存
     * @param inventoryId
     * @return
     */
    Integer getRedisQtyByInventory(String inventoryId);
}
