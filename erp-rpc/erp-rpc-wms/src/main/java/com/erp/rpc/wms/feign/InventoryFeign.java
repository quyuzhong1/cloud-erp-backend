package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @Classname: InventoryFeign
 * @Description: 指定contextId可以使用同一个服务名
 * @CreateTime: 2023-05-05  15:25
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-wms", contextId = "inventory")
public interface InventoryFeign {

    /**
     * 调用说明：
     * 1.调用方如果不是wms，需使用feign远程调用，同时使用seata分布式事务
     */



    /**
     * 根据采购订单生成入库预报单
     * @param dto
     */
    @PostMapping(value = "/feign/instockForcast/generateByPurchaseOrder")
    void generateByPurchaseOrder(@RequestBody @Valid InstockForcastDTO.AddDTO dto);

    /**
     * 采购订单反审核，入库预报处理
     * @param purchaseOrderId
     */
    @PostMapping(value = "/feign/instockForcast/purchaseOrderUnApprove")
    void purchaseOrderUnApprove(@RequestParam(value = "purchaseOrderId")String purchaseOrderId);

    /**
     * 采购订单反审核, 入库预报处理（批量）
     * @param purchaseOrderIds
     */
    @PostMapping(value = "/feign/instockForcast/purchaseOrderUnApproveBatch")
    void purchaseOrderUnApproveBatch(@RequestParam(value = "purchaseOrderIds") List<String> purchaseOrderIds);

    /**
     * 采购订单结束交货
     * @param dto
     */
    @PostMapping(value = "/feign/instockForcast/finishDelivery")
    void finishDelivery(@RequestBody @Valid InstockForcastDTO.FinishDeliveryDTO dto);

    /**
     * 采购订单变更单审核通过
     * @param dto
     */
    @PostMapping(value = "/feign/instockForcast/poChange")
    void poChange(@RequestBody @Valid InstockForcastDTO.PoChangeDTO dto);


    /**
     * 获取sku 库存状态数量（调用方传输状态）特别注意：如果库位没传或者为空，则库位字段会赋值为空查询
     * @author yl
     * @date 2023-05-16 17:17
     * @param paramDTO
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     */
    @PostMapping("feign/inventory/listSkuInventory")
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(@RequestBody InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO);


    /**
     * 获取sku 库存状态数量（调用方传输状态）特别注意：如果库位没传或者为空，则库位字段会赋值为空查询
     * @author yl
     * @date 2023-05-16 17:17
     * @param paramDTO
     * @return java.util.List<com.erp.model.wms.dto.InventoryDTO.SkuInventoryTotalDTO>
     */
    @PostMapping("feign/inventory/listSkuInventoryByParam")
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryByParam(@RequestBody InventoryQtyDTO.SkuInventoryParamDTO paramDTO);

    /**
     * 根据sku获取库存列表
     * @param dto
     * @return
     */
    @PostMapping("feign/inventory/listInventoryBySkuIds")
    List<InventoryEntity> listInventoryBySkuIds(@RequestBody InventoryQtyDTO.InventoryBySkuDTO dto);
   /**
    *获取sku 库存状态数量（调用方传输状态、多状态）特别注意：如果库位没传或者为空，则库位字段会赋值为空查询
    * @author Will
    * @date: 2023/8/21 16:09
    * @param paramDTO
    * @return List<SkuInventoryStatusTotalDTO>
    */
    @PostMapping("feign/inventory/listSkuInventoryStatusByParam")
    List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> listSkuInventoryStatusByParam(@RequestBody InventoryQtyDTO.SkuInventoryStatusParamDTO paramDTO);

    /**
     * 采购订单结束交货（批量）
     * @param dataList
     */
    @PostMapping(value = "/feign/instockForcast/finishDeliveryBatch")
    void finishDeliveryBatch(@RequestBody @Valid List<InstockForcastDTO.FinishDeliveryDTO> dataList);


    /**
     * 根据采购订单生成入库预报单（批量）
     * @param dataList
     */
    @PostMapping(value = "/feign/instockForcast/generateByPurchaseOrderBatch")
    void generateByPurchaseOrderBatch(@RequestBody @Valid List<InstockForcastDTO.AddDTO> dataList);

    /**
     * 采购订单变更单审核通过（批量）
     * @param dataList
     */
    @PostMapping(value = "/feign/instockForcast/poChangeBatch")
    void poChangeBatch(@RequestBody @Valid List<InstockForcastDTO.PoChangeDTO> dataList);

}
