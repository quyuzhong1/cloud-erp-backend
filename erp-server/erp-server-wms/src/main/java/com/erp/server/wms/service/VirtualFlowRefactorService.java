package com.erp.server.wms.service;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;

import java.util.List;

public interface VirtualFlowRefactorService {
    /**
     * 重构
     * @author will
     * @date 2025/3/31 10:19
     * @param jobParam
     */
    void rebuildFlow(String jobParam);
    /**
     * 流水审核
     * @author will
     * @date 2025/4/1 16:04
     * @param dto
     */
    void approve(VirtualInventoryStockDTO.StockParamDTO dto, List<String> ignoreSkuIds,List<BomChildrenSkuDTO> bomChildrenSkuList);

    /**
     * 调拨审核
     * @author will
     * @date 2025/4/2 16:38
     * @param dto
     */
    void approveTransfer(VirtualInventoryStockDTO.TransferParamDTO dto,List<String> ignoreSkuIds);
}
