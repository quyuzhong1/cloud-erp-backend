package com.erp.server.wms.service;

import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;

import java.util.List;

public interface PurchaseOrderSupplierService {
    /**
     * 根据主键Id查询表信息
     * @Author Luo_WG
     * @Date 2023/6/19 15:22
     * @param ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    List<PurchaseOrderSupplierEntity> ListPurchaseOrderSupplierEntityByIds(List<String> ids);

    /**
     * 更新SCM同步过来的采购单供应商表数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdatePurchaseOrderSupplier(List<PurchaseOrderSupplierEntity> purchaseOrderSupplierEntityList);

    /**
     * 根据采购订单获取关系记录
     * @param poIds
     * @return
     */
    List<PurchaseOrderSupplierEntity> listByPurchaseOrderIds(List<String> poIds);
}
