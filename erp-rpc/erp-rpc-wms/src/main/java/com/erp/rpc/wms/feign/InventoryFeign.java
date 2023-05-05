package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferRuleDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Classname: InventoryFeign
 * @Description: 指定contextId可以使用同一个服务名
 * @CreateTime: 2023-05-05  15:25
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-wms", contextId = "inventory")
public interface InventoryFeign {

    /**
     * 出入库业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveInOutStockByType")
    Boolean approveInOutStockByType(@RequestBody @Validated InventoryInStockOrOutStockDTO dto);

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveTransferByType")
    Boolean approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto);

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @PostMapping("/feign/inventory/approveByRule")
    Boolean approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto);

    /**
     * 反审核
     * @param dto
     */
    @PostMapping("/feign/inventory/unApprove")
    Boolean unApprove(@RequestBody @Validated InventoryUnApproveDTO dto);

}
