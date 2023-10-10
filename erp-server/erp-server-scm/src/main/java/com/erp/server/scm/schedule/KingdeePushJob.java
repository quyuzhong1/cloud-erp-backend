package com.erp.server.scm.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.scm.entity.*;
import com.erp.server.scm.kingdee.*;
import com.erp.server.scm.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private SyncKingdeePurchaseOrderService syncKingdeePurchaseOrderService;

    @Resource
    private SyncKingdeePurchaseChangeService syncKingdeePurchaseChangeService;

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
                .in(PurchaseOrderEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PurchaseOrderEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PurchaseOrderEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购订单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeePurchaseOrderService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("采购订单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("采购订单【{}】推送金蝶失败",obj.getCode(),e);
            }
        });

    }

    /**
     * 推送采购订单变更单
     */
    @XxlJob("kingdeePushPurchaseChange")
    public void kingdeePushPurchaseChange() {
        List<PurchaseChangeEntity> list = purchaseChangeService.lambdaQuery()
                .in(PurchaseChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PurchaseChangeEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PurchaseChangeEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购订单变更");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeePurchaseChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("采购订单变更【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("采购订单变更【{}】推送金蝶失败",obj.getCode(),e);
            }
        });

    }

    /**
     * 推送采购价目
     */
    @XxlJob("kingdeePurchasePrice")
    public void kingdeePurchasePrice() {
        List<PurchasePriceEntity> list = purchasePriceService.lambdaQuery()
                .in(PurchasePriceEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PurchasePriceEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PurchasePriceEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购报价");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeePurchasePriceService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("采购价目【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("采购价目【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送采购调价
     */
    @XxlJob("kingdeePurchasePriceChange")
    public void kingdeePurchasePriceChange() {
        List<PurchasePriceChangeEntity> list = purchasePriceChangeService.lambdaQuery()
                .in(PurchasePriceChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PurchasePriceChangeEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PurchasePriceChangeEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购报价");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeePurchasePriceChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("采购调价【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("采购调价【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送委外订单
     */
    @XxlJob("kingdeeSubcontractOrder")
    public void kingdeeSubcontractOrder() {
        List<SubcontractOrderEntity> list = subcontractOrderService.lambdaQuery()
                .in(SubcontractOrderEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SubcontractOrderEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SubcontractOrderEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的委外订单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSubcontractOrderService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("委外订单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("委外订单【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送委外变更单
     */
    @XxlJob("kingdeeSubcontractChange")
    public void kingdeeSubcontractChange() {
        List<SubcontractChangeEntity> list = subcontractChangeService.lambdaQuery()
                .in(SubcontractChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SubcontractChangeEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SubcontractChangeEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的委外变更单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSubcontractChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("委外变更单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("委外变更单【{}】推送金蝶失败",obj.getCode(),e);
            }

        });
    }

    /**
     * 推送供应商
     */
    @XxlJob("kingdeeSupplierService")
    public void kingdeeSupplierService() {
        List<SupplierEntity> list = supplierService.lambdaQuery()
                .in(SupplierEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SupplierEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SupplierEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的供应商");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSupplierService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("供应商【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("供应商【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

}
