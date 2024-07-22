package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;

import java.util.List;

public interface InventoryTradingService {

    // 审核类型：审核
    String APPROVE = "APPROVE";

    // 审核类型：反审核
    String UNAPPROVE = "UNAPPROVE";

    /**
     * 执行库存交易(按列表)
     * @param transactionList   库存交易列表
     * @param approveType    审核类型
     */
    void doTransactionList(List<InventoryTransactionDTO> transactionList, String approveType);
}
