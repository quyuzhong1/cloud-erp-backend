package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.server.wms.service.InventoryService;
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
@RequestMapping("inventory")
public class InventoryFeignController extends BaseController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 出入库业务，按业务类型
     * @param dto
     */
    @PostMapping("/approveInOutStockByType")
    public ApiResult<Void> approveInOutStockByType(@RequestBody @Validated InventoryInStockOrOutStockDTO dto) {
        inventoryService.approveInOutStockByType(dto.getSkus(), InventoryBusinessTypeEnum.of(dto.getBusinessType()));
        return ApiResult.success();
    }

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/approveTransferByType")
    public ApiResult<Void> approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto) {
        inventoryService.approveTransferByType(dto.getSkus(), InventoryBusinessTypeEnum.of(dto.getBusinessType()));
        return ApiResult.success();
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
    public Integer getUsableInventoryTotal(@RequestParam(value = "orgId") String orgId, @RequestParam(value = "warehouseId") String warehouseId,
                                           @RequestParam(value = "skuId") String skuId,@RequestParam(value = "warehouseLocationId", required = false)  String warehouseLocationId) {
        return inventoryService.getUsableInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId);
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
    public Integer getInventoryTotal(@RequestParam(value = "orgId") String orgId, @RequestParam(value = "warehouseId") String warehouseId,
                                     @RequestParam(value = "skuId") String skuId,@RequestParam(value = "warehouseLocationId", required = false)  String warehouseLocationId,
                                     @RequestParam(value = "status") String status) {
        return inventoryService.getInventoryTotal(orgId, warehouseId, skuId, warehouseLocationId, status);
    }

}