package com.erp.server.scm.controller.feign;


import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购订单feign
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@RestController
@RequestMapping("feign/purchaseOrder")
public class PurchaseOrderFeignController {


    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * 根据id查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/getPurchaseOrderById")
    public PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id) {
        return purchaseOrderService.getById(id);
    }

    /**
     * 根据采购订单id查询供应商
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/getOrderSupplierByOrderId")
    public PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id) {
        return purchaseOrderSupplierService.getByPurchaseOrderId(id);
    }

    /**
     * 根据采购订单id查询详情信息
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/listPurchaseOrderDetailById")
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id) {
        return purchaseOrderDetailService.listDetailByIds(id);
    }

    /**
     * @description: 查询供应商信息
     * @author Will
     * @date: 2023/4/14 10:10
     * @param supplierId
     * @return SupplierEntity
     */
    @GetMapping("/getSupplierById")
    public SupplierEntity getSupplierById(@RequestBody String supplierId) {
        return supplierService.getById(supplierId);
    }
}
