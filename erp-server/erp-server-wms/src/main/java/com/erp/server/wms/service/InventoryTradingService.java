package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;

import java.util.List;

public interface InventoryTradingService {

    // 审核类型：审核
    public static final String APPROVE = "APPROVE";

    // 审核类型：反审核
    public static final String UNAPPROVE = "UNAPPROVE";


    void doTransactionList(List<InventoryTransactionDTO> transactionList, String approveType);
}
