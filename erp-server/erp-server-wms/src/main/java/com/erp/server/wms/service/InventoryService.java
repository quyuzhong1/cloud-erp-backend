package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;

import java.util.List;

/**
 * @Classname: InventoryService
 * @Description: TODO
 * @CreateTime: 2023-04-25  12:15
 * @Author: zhangchunlin
 */
public interface InventoryService extends SuperService<InventoryEntity> {

    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    InventoryEntity findInventoryByWareLocalSkuStatus(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);

    /**
     * 出入库业务，审批出入库单据，按业务类型
     * @param paramList
     * @param businessType
     */
    void approveInOutStockByType(List<InStockOrOutStockDTO> paramList, InventoryBusinessTypeEnum businessType);

    /**
     * 调拨业务，审批出入库单据，按业务类型
     * @param paramList
     * @param businessType
     */
    void approveTransferByType(List<InventoryTransferDTO> paramList, InventoryBusinessTypeEnum businessType);

    /**
     * 调拨业务，按交易规则
     * @param paramList
     * @param businessType
     */
    void approveByRule(List<InventoryTransferDTO> paramList, List<InventoryTransferDTO> ruleList, InventoryBusinessTypeEnum businessType);

    /**
     * 按单据ID反审核
     * @param billId
     */
    void unApprove(String billId);

}
