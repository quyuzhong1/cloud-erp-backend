package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.TransactionFlowDTO;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryModeEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Classname: TransactionFlowService
 * @Description: TODO
 * @CreateTime: 2023-04-25  19:43
 * @Author: zhangchunlin
 */
public interface TransactionFlowService extends SuperService<TransactionFlowEntity> {

    /**
     * 根据单据来源和单据id查询出库存交易流水
     * @param sourceType
     * @param sourceId
     * @return
     */
    List<TransactionFlowEntity> getUnApprovedTxnFlows(String sourceType, String sourceId);

    /**
     * 修改交易流水为已反审核
     * @param id
     * @param version
     * @return
     */
    int updateUnapprovedById(String id, Integer version);

    /**
     * 记录库存交易流水
     */
    void recordFlowTransaction(TransactionFlowDTO param, InventoryBusinessTypeEnum businessType,
                                      String transactionRuleId, Integer afterInventoryQty, InventoryModeEnum inventoryModeEnum);

}
