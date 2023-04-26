package com.erp.server.scm.controller.feign;


import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.*;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 采购订单feign
 *
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

    @Resource
    private SupplierContactService supplierContactService;

    /**
     * 根据id查询采购订单
     *
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getPurchaseOrderById")
    public PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id) {
        return purchaseOrderService.getById(id);
    }


    /**
     * 根据ids查询采购订单
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderByIds")
    public List<PurchaseOrderEntity> listPurchaseOrderByIds(@RequestBody List<String> ids) {
        return purchaseOrderService.listByIds(ids);
    }

    /**
     * 根据采购订单id查询供应商
     *
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getOrderSupplierByOrderId")
    public PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id) {
        return purchaseOrderSupplierService.getByPurchaseOrderId(id);
    }


    /**
     * 根据采购订单id 获取供应商仓库信息
     *
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getByOrderId")
    public  PurchaseOrderDTO.GetOneDTO getByOrderId(@RequestBody String id) {
        return purchaseOrderService.getPurchaseOrder(id);
    }


    /**
     * 根据采购订单ids 获取供应商仓库信息
     *
     * @param purchaseOrderIds：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/getByOrderIds")
    public  List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getByOrderId(@RequestBody List<String> purchaseOrderIds) {
        return purchaseOrderService.getPurchaseOrderByOrderIds(purchaseOrderIds);
    }

    /**
     * 根据采购订单详情id查询详情信息
     *
     * @param  id：采购订单详情表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderDetailById")
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id) {
        return purchaseOrderDetailService.listDetailByIds(id);
    }
    /**
     * @description: 根据采购订单ids查询明细
     * @author Will
     * @date: 2023/4/24 19:43
     * @param ids
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("/listByPurchaseOrderIds")
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(@RequestBody List<String> ids) {
        return purchaseOrderDetailService.listByPurchaseOrderIds(ids);
    }



    /**
     * 根据采购订单id查询详情信息
     * @param  id：采购订单表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("/listPurchaseOrderDetailByOrderId")
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(@RequestBody String id) {
        return purchaseOrderDetailService.listPurchaseOrderDetailByOrderId(id);
    }

    /**
     * @param supplierId
     * @return SupplierEntity
     * @description: 查询供应商信息
     * @author Will
     * @date: 2023/4/14 10:10
     */
    @PostMapping("/getSupplierById")
    public SupplierEntity getSupplierById(@RequestBody String supplierId) {
        return supplierService.getById(supplierId);
    }

    /**
     * @param supplierIds
     * @return SupplierEntity
     * @description: 查询供应商信息
     * @author Will
     * @date: 2023/4/14 10:10
     */
    @PostMapping("/getSupplierByIdList")
    public List<SupplierEntity> getSupplierById(@RequestBody List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return Collections.emptyList();
        }
        return supplierService.listByIds(supplierIds);
    }

    /**
     * 根据联系人id查询供应商联系人信息
     *
     * @param supplierContactId supplierContactId
     * @return com.erp.model.scm.entity.SupplierEntity
     * @Author Luo_WG
     * @Date 2023/4/17 11:00
     **/
    @PostMapping("/getSupplierContactById")
    public SupplierContactEntity getSupplierContactById(@RequestBody String supplierContactId) {
        return supplierContactService.getById(supplierContactId);
    }


    /**
     * 修改采购订单明细表
     * @Author Luo_WG
     * @Date 2023/4/20 18:51
     * @param entity entity
     * @return java.lang.Boolean
     **/
    @PostMapping("/updatePurchaseOrderDetailById")
    public Boolean updatePurchaseOrderDetailById(@RequestBody PurchaseOrderDetailEntity entity) {
        return purchaseOrderDetailService.updateById(entity);
    }

    /**
     * 批量修改采购订单明细表
     * @Author Luo_WG
     * @Date 2023/4/20 18:51
     * @param entityList entityList
     * @return java.lang.Boolean
     **/
    @PostMapping("/updatePurchaseOrderDetailByIdBatch")
    public Boolean updatePurchaseOrderDetailByIdBatch(@RequestBody List<PurchaseOrderDetailEntity> entityList) {
        return purchaseOrderDetailService.updateBatchById(entityList);
    }
}
