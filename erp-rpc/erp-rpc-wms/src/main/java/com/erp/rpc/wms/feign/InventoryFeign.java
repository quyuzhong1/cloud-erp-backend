package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.inventory.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
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
     * 出入库业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveInOutStockByType")
    void approveInOutStockByType(@RequestBody @Validated InventoryInOutStockDTO dto);

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveTransferByType")
    void approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto);

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @PostMapping("/feign/inventory/approveByRule")
    void approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto);

    /**
     * 出入库业务，自定义规则
     *
     * @param dto
     */
    @PostMapping("/feign/inventory/approveInOutStockByRule")
    void approveInOutStockByRule(@RequestBody @Validated InventoryInOutStockRuleDTO dto);

    /**
     * 反审核
     * @param dto
     */
    @PostMapping("/feign/inventory/unApprove")
    void unApprove(@RequestBody @Validated InventoryUnApproveDTO dto);


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
     * 批量反审核
     * @param dto
     */
    @PostMapping("/feign/inventory/batchUnApprove")
    void batchUnApprove(@RequestBody @Validated InventoryBatchUnApproveDTO dto);

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

}
