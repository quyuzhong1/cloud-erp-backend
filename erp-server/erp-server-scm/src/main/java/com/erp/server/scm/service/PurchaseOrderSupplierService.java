package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderSupplierService extends SuperService<PurchaseOrderSupplierEntity> {
    /**
     * @description: 根据采购订单ids查询
     * @author Will
     * @date: 2023/3/28 17:25
     * @param purchaseOrderIds

     */
    void deleteByPurchaseOrderIds(List<String> purchaseOrderIds);
    /**
     * @description: 根据订单主表id查询供应商
     * @author Will
     * @date: 2023/3/28 17:28
     * @param purchaseOrderId
     * @return PurchaseOrderSupplierEntity
     */
    PurchaseOrderSupplierEntity listByPurchaseOrderId(String purchaseOrderId);
}
