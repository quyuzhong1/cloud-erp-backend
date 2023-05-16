package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.InventoryDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.InventoryEntity;

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
    InventoryEntity findInventory(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);


    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录，带分布式锁，会根据是否控制库位查询（不控制库位则将库位查询条件置为空字符串）
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    InventoryEntity findInventoryLock(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);


    /**
     * 根据组织、仓库、库位、状态判断库存数据；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    List<InventoryEntity> findInventoryCheckLocation(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);

    /**
     * 根据组织、仓库、库位、状态获取可用库存数量；如果库位为空，则赋值空库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @return
     */
    Integer getUsableInventoryTotal(String orgId,String warehouseId,String skuId, String warehouseLocationId);



    /**
     * 根据skuIds 仓库 ，组织 仓位 获取到 sku即时库存
     * @author yl
     * @date 2023-05-16 17:06
     * @param skuIds
     * @param warehouseId
     * @param orgId
     * @param warehouseLocationId
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     */
    List<InventoryDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds,String warehouseId,String orgId, String warehouseLocationId);

    /**
     * 根据组织、仓库、库位、状态、SKU获取库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @param status
     * @return
     */
    Integer getInventoryTotal(String orgId,String warehouseId,String skuId, String warehouseLocationId,String status);

    /**
     * 修改库存表数量
     * @param id
     * @param qty
     * @param version
     * @return
     */
    int updateQtyById(String id, Integer qty, Integer version);

    /**
     * @description: 查询拣货数据
     * @author Will
     * @date: 2023/5/12 15:48
     * @param dto
     * @return List<InventoryEntity>
     */
    List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto);

}
