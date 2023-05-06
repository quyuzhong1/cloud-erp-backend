package com.erp.rpc.wms.feign;

import com.common.core.controller.vo.ApiResult;
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
     * 调用说明：
     * 1.调用方如果不是wms，需使用feign远程调用，同时使用seata分布式事务
     * 2.由于存在全局异常拦截，需拦截远程调用方的异常信息，使用ResultUtil.checkRemoteResult
     */

    /**
     * 出入库业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveInOutStockByType")
    ApiResult<Void> approveInOutStockByType(@RequestBody @Validated InventoryInStockOrOutStockDTO dto);

    /**
     * 调拨业务，按业务类型
     * @param dto
     */
    @PostMapping("/feign/inventory/approveTransferByType")
    ApiResult<Void> approveTransferByType(@RequestBody @Validated InventoryTransferDTO dto);

    /**
     * 调拨业务，自定义规则
     * @param dto
     */
    @PostMapping("/feign/inventory/approveByRule")
    ApiResult<Void> approveByRule(@RequestBody @Validated InventoryTransferRuleDTO dto);

    /**
     * 反审核
     * @param dto
     */
    @PostMapping("/feign/inventory/unApprove")
    ApiResult<Void> unApprove(@RequestBody @Validated InventoryUnApproveDTO dto);

}
