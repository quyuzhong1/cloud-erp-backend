package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.*;

/**
 * 虚拟库存交易核心处理类
 * @author will
 * @date 2024/6/5 17:45
 */
public interface VirtualInventoryTransCoreService {
    String BUSINESS_TYPE = "WMS_VIRTUAL_INVENTORY_SKU";

    /**
     * 入库/出库 审核，按业务类型 自动匹配库存规则
     * @param dto 出入库业务参数
     */
    void approve(VirtualInventoryStockDTO.StockParamDTO dto);

    /**
     * 调拨业务 审核，按业务类型 自动匹配库存规则
     * @param dto 调拨业务参数
     */
    void approve(VirtualInventoryStockDTO.TransferParamDTO dto);

    /**
     * 单据 反审核
     * @param dto 业务参数
     */
    void unApprove(InventoryUnApproveDTO dto);

    /**
     * 批量反审核
     * @param dto 业务参数
     */
    void batchUnApprove(InventoryBatchUnApproveDTO dto);


}
