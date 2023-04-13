package com.erp.server.scm.controller.feign;


import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import org.springframework.web.bind.annotation.*;

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
     * 根据采购订单id查询供应商
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @GetMapping("/listPurchaseOrdeDetailrById")
    public List<PurchaseOrderDetailEntity> listPurchaseOrdeDetailrById(@RequestBody List<String> id) {
        return purchaseOrderDetailService.listDetailByIds(id);
    }
}
