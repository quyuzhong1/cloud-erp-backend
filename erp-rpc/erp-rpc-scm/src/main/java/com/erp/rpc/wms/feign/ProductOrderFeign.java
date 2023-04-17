package com.erp.rpc.wms.feign;

import com.erp.model.scm.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

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

    /**
     * 根据采购订单id查询供应商
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("feign/purchaseOrder/getOrderSupplierByOrderId")
    PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id);

    /**
     * 根据采购订单id查询详情信息
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/listPurchaseOrderDetailById")
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id);
    /**
     * @description: 根据id查询供应商
     * @author Will
     * @date: 2023/4/14 10:07
     * @param supplierId
     * @return SupplierEntity
     */
    @PostMapping("/getSupplierById")
    SupplierEntity getSupplierById(@RequestBody String supplierId);

    /**
     * 根据联系人id查询供应商联系人信息
     * @Author Luo_WG
     * @Date 2023/4/17 11:00
     * @param supplierContactId supplierContactId
     * @return com.erp.model.scm.entity.SupplierEntity
     **/
    @PostMapping("feign/purchaseOrder/getSupplierContactById")
    SupplierContactEntity getSupplierContactById(@RequestBody String supplierContactId);
}
