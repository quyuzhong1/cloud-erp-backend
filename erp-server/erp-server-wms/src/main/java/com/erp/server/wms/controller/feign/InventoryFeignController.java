package com.erp.server.wms.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferRuleDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.server.wms.config.InventoryHelper;
import com.erp.server.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    @Resource
    private InventoryHelper inventoryHelper;

    /**
     * 出入库业务，按业务类型
     * @param dto
     */
    @PostMapping("/approveInOutStockByType")
    public Boolean approveInOutStockByType(@RequestBody @Validated InventoryInStockOrOutStockDTO dto) {
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK).approve(dto.getSkus(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
        return Boolean.TRUE;
    }

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/approveTransferByType")
    public Boolean approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto) {
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK).approve(dto.getSkus(), null, InventoryBusinessTypeEnum.of(dto.getBusinessType()), true);
        return Boolean.TRUE;
    }

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @PostMapping("/approveByRule")
    public Boolean approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto) {
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK).approve(dto.getSkus(), dto.getRules(), InventoryBusinessTypeEnum.of(dto.getBusinessType()), false);
        return Boolean.TRUE;
    }

    /**
     * 反审核
     * @param dto
     */
    @PostMapping("/unApprove")
    public Boolean unApprove(@RequestBody @Validated InventoryUnApproveDTO dto) {
        inventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK).unApprove(dto);
        return Boolean.TRUE;
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