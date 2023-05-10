package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

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
     * 采购订单结束交货
     * @param dto
     */
    @PostMapping(value = "/feign/instockForcast/finishDelivery")
    void finishDelivery(@RequestBody @Valid InstockForcastDTO.FinishDeliveryDTO dto);

}
