package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTransCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Classname: InventoryFeignController
 * @Description: TODO
 * @CreateTime: 2023-04-27  10:31
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping("/feign/inventory")
public class InventoryFeignController extends BaseController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    /**
     * 出入库业务，按业务类型
     *
     * @param dto
     */
    @PostMapping("/approveInOutStockByType")
    public void approveInOutStockByType(@RequestBody @Validated InventoryInOutStockDTO dto) {
        inventoryTransCoreService.approveByType(dto);
    }

    /**
     * 调拨业务，按业务类型
     *
     * @param dto
     */
    @PostMapping("/approveTransferByType")
    public void approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto) {
        inventoryTransCoreService.approveByType(dto);
    }

    /**
     * 调拨业务，自定义规则
     *
     * @param dto
     */
    @PostMapping("/approveByRule")
    public void approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto) {
        inventoryTransCoreService.approveByRule(dto);
    }

    /**
     * 出入库业务，自定义规则
     *
     * @param dto
     */
    @PostMapping("/approveInOutStockByRule")
    public void approveInOutStockByRule(@RequestBody @Validated InventoryInOutStockRuleDTO dto) {
        inventoryTransCoreService.approveByRule(dto);
    }

    /**
     * 反审核
     *
     * @param dto
     */
    @PostMapping("/unApprove")
    public void unApprove(@RequestBody @Validated InventoryUnApproveDTO dto) {
        inventoryTransCoreService.unApprove(dto);
    }

    /**
     * 根据组织、仓库、库位、状态获取库存数量；特别注意：如果库位没传或者为空，则库位字段会赋值为空查询
     *
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @return
     */
    @PostMapping("/getInventoryTotal")
    public Integer getInventoryTotal(@RequestParam(value = "orgId") String orgId, @RequestParam(value = "warehouseId") String warehouseId,
                                     @RequestParam(value = "skuId") String skuId, @RequestParam(value = "warehouseLocationId", required = false) String warehouseLocationId,
                                     @RequestParam(value = "status") String status) {
        return inventoryService.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, status);
    }


    /**
     * 批量反审核
     *
     * @param dto
     */
    @PostMapping("/batchUnApprove")
    public void batchUnApprove(@RequestBody @Validated InventoryBatchUnApproveDTO dto) {
        inventoryTransCoreService.batchUnApprove(dto);
    }


    /**
     * 获取sku 的即时库存（调用方传入状态）；特别注意：如果库位没传或者为空，则库位字段会赋值为空查询
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-05-16 17:20
     */
    @PostMapping("/listSkuInventory")
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(@RequestBody @Valid InventoryQtyDTO.FindSkuInventoryParamDTO dto) {
        List<InventoryQtyDTO.SkuInventoryTotalDTO> resultList = inventoryService.listSkuInventory(dto.getSkuIds(), dto.getWarehouseId(), dto.getWarehouseLocationId(), dto.getInventoryStatus());
        return resultList;
    }


    @PostMapping("/listSkuInventoryByParam")
    public List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryByParam(@RequestBody @Valid InventoryQtyDTO.SkuInventoryParamDTO dto) {
        List<InventoryQtyDTO.SkuInventoryTotalDTO> resultList = inventoryService.listSkuInventory(dto);
        return resultList;
    }


}