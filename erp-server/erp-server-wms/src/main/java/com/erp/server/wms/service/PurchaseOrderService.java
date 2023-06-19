package com.erp.server.wms.service;

import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;

import java.util.List;

public interface PurchaseOrderService {
    /**
     * 更新SCM同步过来的采购单主表数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdatePurchaseOrder(List<PurchaseOrderEntity> purchaseOrderEntityList);
}
