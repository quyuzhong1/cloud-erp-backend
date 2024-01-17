package com.erp.server.scm.schedule;

import com.erp.server.scm.rocketmq.sync.wms.WmsSyncPurchaseService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * scm定时器
 * @Author Luo_WG
 * @Date 2023/6/19 12:59
 **/
@Component
@Slf4j
public class ScmJob {
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


    /**
     * 采购订单自动确认
     */
    @XxlJob("purchaseOrderAutoConfirm")
    public void purchaseOrderAutoConfirm() {
        XxlJobHelper.log("=====采购订单自动确认 开始任务=====");
        long start = System.currentTimeMillis();
        purchaseOrderService.purchaseOrderAutoConfirm();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====采购订单自动确认 结束任务=====");
    }
}
