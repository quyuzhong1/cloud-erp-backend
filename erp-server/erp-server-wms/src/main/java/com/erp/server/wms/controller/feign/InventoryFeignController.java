package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferRuleDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTransCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * @param dto
     */
    @PostMapping("/approveInOutStockByType")
    public ApiResult<Void> approveInOutStockByType(@RequestBody @Validated InventoryInStockOrOutStockDTO dto) {
        inventoryTransCoreService.approveInOutStockByType(dto);
        return success();
    }

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/approveTransferByType")
    public ApiResult<Void> approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto) {
        inventoryTransCoreService.approveTransferByType(dto);
        return success();
    }

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @PostMapping("/approveByRule")
    public ApiResult<Void> approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto) {
        inventoryTransCoreService.approveByRule(dto);
        return success();
    }

    /**
     * 反审核
     * @param dto
     */
    @PostMapping("/unApprove")
    public ApiResult<Void> unApprove(@RequestBody @Validated InventoryUnApproveDTO dto) {
        inventoryTransCoreService.unApprove(dto);
        return success();
    }


    /**
     * 根据组织、仓库、库位、状态获取可用库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @return
     */
    @PostMapping("/getUsableInventoryTotal")
    public ApiResult<Integer> getUsableInventoryTotal(@RequestParam(value = "orgId") String orgId, @RequestParam(value = "warehouseId") String warehouseId,
                                           @RequestParam(value = "skuId") String skuId,@RequestParam(value = "warehouseLocationId", required = false)  String warehouseLocationId) {
        return success(inventoryService.getUsableInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId));
    }

    /**
     * 根据组织、仓库、库位、状态获取库存数量；如果库位为空，则不判断库位
     * @param orgId
     * @param warehouseId
     * @param skuId
     * @param warehouseLocationId
     * @return
     */
    @PostMapping("/getInventoryTotal")
    public ApiResult<Integer> getInventoryTotal(@RequestParam(value = "orgId") String orgId, @RequestParam(value = "warehouseId") String warehouseId,
                                     @RequestParam(value = "skuId") String skuId,@RequestParam(value = "warehouseLocationId", required = false)  String warehouseLocationId,
                                     @RequestParam(value = "status") String status) {
        return success(inventoryService.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, status));
    }

}