package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;

import java.util.List;

public interface VirtualInventoryTradingService {
    /**
     * 执行库存交易
     * @author will
     * @date 2025/11/24 16:41
     * @param transactionDtoList
     * @param approveType
     * @return void
     */
    void doTransactionList(List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionDtoList, String approveType);
}
