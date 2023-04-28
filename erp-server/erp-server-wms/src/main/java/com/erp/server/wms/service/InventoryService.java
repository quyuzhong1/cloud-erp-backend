package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.dto.inventory.TransferDTO;
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
     * 根据组织、仓库、库位、状态判断库存是否存在记录（库位为空也作为条件）
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    InventoryEntity findInventoryByWareLocalSkuStatus(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);


    /**
     * 根据组织、仓库、库位、状态判断库存数据；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    List<InventoryEntity> findInventoryByWareSkuStatusCheckLocation(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);

    /**
     * 根据组织、仓库、库位、状态获取可用库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @return
     */
    Integer getUsableInventoryTotal(String orgId,String warehouseId,String skuId, String warehouseLocationId);

    /**
     * 根据组织、仓库、库位、状态获取库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    Integer getInventoryTotal(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);

    /**
     * 出入库业务，审批出入库单据，按业务类型
     * @param paramList
     * @param businessType
     */
    void approveInOutStockByType(List<InStockOrOutStockDTO> paramList, InventoryBusinessTypeEnum businessType);

    /**
     * 调拨业务，审批出入库单据，按业务类型（直接调拨单、库存调整单）
     * @param paramList
     * @param businessType
     */
    void approveTransferByType(List<TransferDTO> paramList, InventoryBusinessTypeEnum businessType);

    /**
     * 调拨业务，按交易规则
     * @param paramList
     * @param businessType
     */
    void approveByRule(List<TransferDTO> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType);

    /**
     * 按单据ID反审核
     * @param dto
     */
    void unApprove(InventoryUnApproveDTO dto);

}
