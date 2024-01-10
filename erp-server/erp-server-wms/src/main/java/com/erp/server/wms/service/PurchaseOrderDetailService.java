package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;

import java.util.List;

public interface PurchaseOrderDetailService extends SuperService<PurchaseOrderDetailEntity> {

    /**
     * 根据主键Id查询表信息
     * @Author Luo_WG
     * @Date 2023/6/19 15:21
     * @param ids
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    List<PurchaseOrderDetailEntity> ListProductDetailEntityByIds(List<String> ids);

    /**
     * 更新SCM同步过来的采购单详情表数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    Boolean saveOrUpdatePurchaseOrderDetail(List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList);
}
