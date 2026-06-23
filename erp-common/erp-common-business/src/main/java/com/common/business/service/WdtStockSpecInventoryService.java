package com.common.business.service;

/**
 * 旺店通实体仓可用库存查询（StockSpecAPI）
 */
public interface WdtStockSpecInventoryService {

    /**
     * 查询实体仓 SKU 可用库存
     *
     * @param warehouseNo 旺店通实体仓编码
     * @param specNo      SKU 编码
     * @return 可用库存，未查到返回 0
     */
    Integer queryAvailableStock(String warehouseNo, String specNo);
}
