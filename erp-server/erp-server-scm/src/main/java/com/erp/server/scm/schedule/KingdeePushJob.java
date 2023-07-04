package com.erp.server.scm.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.kingdee.SyncKingdeePurchaseOrderService;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceChangeService;
import com.erp.server.scm.service.PurchaseOrderService;
import com.erp.server.scm.service.PurchasePriceChangeService;
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
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private SyncKingdeePurchasePriceChangeService syncKingdeePurchasePriceChangeService;

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
     * 推送采购报价
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
}
