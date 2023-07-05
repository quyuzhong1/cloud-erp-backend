package com.erp.server.scm.rocketmq.sync.wms;

import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;

import java.util.List;

public interface WmsSyncPurchaseService {
    /**
     * 同步采购单信息表数据到scm
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncPurchaseOrderToWms(List<PurchaseOrderEntity> list);

    /**
     * 同步采购单详情信息表数据到scm
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncPurchaseOrderDetailToWms(List<PurchaseOrderDetailEntity> list);


    /**
     * 同步采购单供应商信息表数据到wms
     * @Author Luo_WG
     * @Date 2023/4/19 14:03
     **/
    void syncPurchaseOrderSupplierToWms(List<PurchaseOrderSupplierEntity> list);
}
