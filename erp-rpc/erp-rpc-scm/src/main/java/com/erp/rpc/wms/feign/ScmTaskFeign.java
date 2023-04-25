package com.erp.rpc.wms.feign;

import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.workflow.dto.WorkOptionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 采购订单feign
 * @Author Luo_WG
 * @Date 2023/4/13 11:41
 **/
@FeignClient(name = "erp-scm")
public interface ScmTaskFeign {

    /**
     * 根据id查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/getPurchaseOrderById")
    PurchaseOrderEntity getPurchaseOrderById(@RequestBody String id);

    /**
     * 根据ids查询采购订单
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderByIds")
    List<PurchaseOrderEntity> listPurchaseOrderByIds(@RequestBody List<String> ids);



    /**
     * 根据采购订单id查询供应商
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/getOrderSupplierByOrderId")
    PurchaseOrderSupplierEntity getOrderSupplierByOrderId(@RequestBody String id);

    /**
     * 根据采购订单详情id查询详情信息
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     * @param id id：采购订单详情表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderDetailById")
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailById(@RequestBody List<String> id);
    /**
     * @description: 根据id查询供应商
     * @author Will
     * @date: 2023/4/14 10:07
     * @param supplierId
     * @return SupplierEntity
     */
    @PostMapping("feign/purchaseOrder/getSupplierById")
    SupplierEntity getSupplierById(@RequestBody String supplierId);

    /**
     * 根据ids查询供应商
     * @description:
     * @author Will
     * @date: 2023/4/14 10:07
     * @param supplierIds
     * @return SupplierEntity
     */
    @PostMapping("feign/purchaseOrder/getSupplierByIdList")
    List<SupplierEntity> getSupplierByIdList(@RequestBody List<String> supplierIds);

    /**
     * 根据联系人id查询供应商联系人信息
     * @Author Luo_WG
     * @Date 2023/4/17 11:00
     * @param supplierContactId supplierContactId
     * @return com.erp.model.scm.entity.SupplierEntity
     **/
    @PostMapping("feign/purchaseOrder/getSupplierContactById")
    SupplierContactEntity getSupplierContactById(@RequestBody String supplierContactId);


    /**
     * 根据采购订单id获取到
     * 采购对应的信息
     * @author yl
     * @date 2023-04-19 17:23
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO
     */
    @PostMapping("feign/purchaseOrder/getByOrderId")
    PurchaseOrderDTO.GetOneDTO getByOrderId(@RequestBody String purchaseOrderId);

    /**
     * 根据采购订单id获取到
     * 采购对应的信息
     * @author yl
     * @date 2023-04-19 17:23
     * @param purchaseOrderIds
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO
     */
    @PostMapping("feign/purchaseOrder/getByOrderIds")
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO >getByOrderIds(@RequestBody List<String> purchaseOrderIds);

    /**
     * @description: 根据采购订单ids查询详情信息
     * @author Will
     * @date: 2023/4/24 19:44
     * @param purchaseOrderIds
     * @return List<PurchaseOrderDetailEntity>
     */
    @PostMapping("feign/purchaseOrder/listByPurchaseOrderIds")
    List<PurchaseOrderDetailEntity>listByPurchaseOrderIds(@RequestBody List<String> purchaseOrderIds);

    /**
     * 根据采购订单id查询详情信息
     * @param  id：采购订单表id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/listPurchaseOrderDetailByOrderId")
    List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(@RequestBody String id);

    /**
     * 修改采购订单明细表
     * @param  entity entity
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     * @Author Luo_WG
     * @Date 2023/4/13 11:20
     **/
    @PostMapping("feign/purchaseOrder/updatePurchaseOrderDetailById")
    Boolean updatePurchaseOrderDetailById(@RequestBody PurchaseOrderDetailEntity entity);

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("feign/scmWorkOption/getTableNum")
    Integer getTableNum(@RequestBody WorkOptionDTO.TableNumDTO tableNumDTO);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/syncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params);
}
