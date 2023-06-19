package com.erp.server.scm.schedule;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.mq.sync.wms.WmsSyncPurchaseService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * scm定时器
 * @Author Luo_WG
 * @Date 2023/6/19 12:59
 **/
@Component
@Slf4j
public class scmJob {
    @Resource
    private WmsSyncPurchaseService WmsSyncPurchaseService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    /**
     * 同步采购信息到wms
     */
    @XxlJob("syncPurchaseToWms")
    public void syncPurchaseToWms() {
        WmsSyncPurchaseService.syncPurchaseOrderToWms(purchaseOrderService.list());
        WmsSyncPurchaseService.syncPurchaseOrderDetailToWms(purchaseOrderDetailService.list());
        WmsSyncPurchaseService.syncPurchaseOrderSupplierToWms(purchaseOrderSupplierService.list());
    }

    @XxlJob("productSkuSyncDmp")
    public void syncPurchaseOrderDetailToWms() {

    }

    @XxlJob("productSkuSyncDmp")
    public void syncPurchaseOrderSupplierToWms() {

    }
}
