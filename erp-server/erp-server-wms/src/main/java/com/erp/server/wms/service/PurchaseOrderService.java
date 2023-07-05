package com.erp.server.wms.service;

import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;

import java.util.List;

public interface PurchaseOrderService {
    /**
     * 根据主键Id查询表信息
     * @Author Luo_WG
     * @Date 2023/6/19 15:22
     * @param ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderEntity>
     **/
    List<PurchaseOrderEntity> ListPurchaseOrderEntityByIds(List<String> ids);

    /**
     * 更新SCM同步过来的采购单主表数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdatePurchaseOrder(List<PurchaseOrderEntity> purchaseOrderEntityList);
}
