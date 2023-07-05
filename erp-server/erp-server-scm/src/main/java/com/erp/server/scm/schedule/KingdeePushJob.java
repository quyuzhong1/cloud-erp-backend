package com.erp.server.scm.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.erp.model.scm.entity.*;
import com.erp.server.scm.kingdee.*;
import com.erp.server.scm.service.*;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2023/1/12 15:19
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeePushJob {

   @Resource
   private PurchaseOrderService purchaseOrderService;

    @Resource
    private SyncKingdeePurchaseOrderService syncKingdeePurchaseOrderService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private SyncKingdeePurchasePriceChangeService syncKingdeePurchasePriceChangeService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SyncKingdeeSubcontractOrderService syncKingdeeSubcontractOrderService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SyncKingdeeSubcontractChangeService syncKingdeeSubcontractChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SyncKingdeeSupplierService syncKingdeeSupplierService;


    /**
     * 推送采购订单
     */
    @XxlJob("kingdeePushPurchaseOrder")
    public void kingdeePushPurchaseOrder() {
        List<PurchaseOrderEntity> list = purchaseOrderService.lambdaQuery()
                .in(PurchaseOrderEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购订单");
            return;
        }
        list.forEach(obj->{
            syncKingdeePurchaseOrderService.syncDataToKingdee(obj, obj.getSyncOperate());
        });

    }

    /**
     * 推送采购价目
     */
    @XxlJob("kingdeePurchasePrice")
    public void kingdeePurchasePrice() {
        List<PurchasePriceEntity> list = purchasePriceService.lambdaQuery()
                .in(PurchasePriceEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购报价");
            return;
        }
        list.forEach(obj->{
            syncKingdeePurchasePriceService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送采购调价
     */
    @XxlJob("kingdeePurchasePriceChange")
    public void kingdeePurchasePriceChange() {
        List<PurchasePriceChangeEntity> list = purchasePriceChangeService.lambdaQuery()
                .in(PurchasePriceChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购报价");
            return;
        }
        list.forEach(obj->{
            syncKingdeePurchasePriceChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送委外订单
     */
    @XxlJob("kingdeeSubcontractOrder")
    public void kingdeeSubcontractOrder() {
        List<SubcontractOrderEntity> list = subcontractOrderService.lambdaQuery()
                .in(SubcontractOrderEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的委外订单");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSubcontractOrderService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送委外变更单
     */
    @XxlJob("kingdeeSubcontractChange")
    public void kingdeeSubcontractChange() {
        List<SubcontractChangeEntity> list = subcontractChangeService.lambdaQuery()
                .in(SubcontractChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的委外变更单");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSubcontractChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送供应商
     */
    @XxlJob("kingdeeSupplierService")
    public void kingdeeSupplierService() {
        List<SupplierEntity> list = supplierService.lambdaQuery()
                .in(SupplierEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的供应商");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSupplierService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

}
