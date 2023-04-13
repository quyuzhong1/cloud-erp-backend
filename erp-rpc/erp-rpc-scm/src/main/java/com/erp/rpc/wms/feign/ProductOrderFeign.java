package com.erp.rpc.wms.feign;

import com.erp.model.scm.entity.PurchaseOrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 采购订单feign
 * @Author Luo_WG
 * @Date 2023/4/13 11:41
 **/
@FeignClient(name = "erp-scm")
public interface ProductOrderFeign {

    /**
     * 根据id查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("feign/purchaseOrder/getPurchaseOrderById")
    PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id);
}
