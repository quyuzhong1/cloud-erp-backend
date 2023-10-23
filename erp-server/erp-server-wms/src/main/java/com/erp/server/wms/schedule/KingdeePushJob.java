package com.erp.server.wms.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.kingdee.*;
import com.erp.server.wms.service.*;
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
    private MachineInfoService machineInfoService;

    @Resource
    private SyncKingdeeMachineInfoService syncKingdeeMachineInfoService;

    @Resource
    private OtherOutstockService otherOutstockService;

    @Resource
    private SyncKingdeeOtherOutstockService syncKingdeeOtherOutstockService;

    @Resource
    private OtherInstockService otherInstockService;

    @Resource
    private SyncKingdeeOtherInstockService syncKingdeeOtherInstockService;

    @Resource
    private PurchaseReturnOrderService purchaseReturnOrderService;

    @Resource
    private SyncKingdeeReturnOrderService syncKingdeeReturnOrderService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private SyncKingdeeStockInService syncKingdeeStockInService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private SyncKingdeeTransferInfoService syncKingdeeTransferInfoService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SyncKingdeeWarehouseService syncKingdeeWarehouseService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SyncKingdeeSoReturnService syncKingdeeSoReturnService;

    @Resource
    private SyncKingdeePoReceiveService syncKingdeePoReceiveService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    /**
     * 推送加工单
     */
    @XxlJob("kingdeePushMachineInfo")
    public void kingdeePushMachineInfo() {
        List<MachineInfoEntity> list = machineInfoService.lambdaQuery()
                .in(MachineInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(MachineInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(MachineInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的加工单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeMachineInfoService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }
        });

    }

    /**
     * 推送其他出库
     */
    @XxlJob("kingdeePushOtherOutstock")
    public void kingdeePushOtherOutstock() {
        List<OtherOutstockEntity> list = otherOutstockService.lambdaQuery()
                .in(OtherOutstockEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(OtherOutstockEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(OtherOutstockEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的其他出库");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeOtherOutstockService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送其他入库
     */
    @XxlJob("kingdeePushOtherInstock")
    public void kingdeePushOtherInstock() {
        List<OtherInstockEntity> list = otherInstockService.lambdaQuery()
                .in(OtherInstockEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(OtherInstockEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(OtherInstockEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的其他入库");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeOtherInstockService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }

        });
    }

    /**
     * 推送采购退货
     */
    @XxlJob("kingdeePurchaseReturnOrder")
    public void kingdeePurchaseReturnOrder() {
        List<PurchaseReturnOrderEntity> list = purchaseReturnOrderService.lambdaQuery()
                .in(PurchaseReturnOrderEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PurchaseReturnOrderEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PurchaseReturnOrderEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购退货");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeReturnOrderService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送销售出库单
     */
    @XxlJob("kingdeePushSoOutstock")
    public void kingdeePushSoOutstock() {
        List<SoOutstockEntity> list = soOutstockService.lambdaQuery()
                .in(SoOutstockEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SoOutstockEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SoOutstockEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售出库单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSoOutstockService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }

        });
    }

    /**
     * 推送采购入库单
     */
    @XxlJob("kingdeePushPoInstock")
    public void kingdeePushPoInstock() {
        List<PoInstockEntity> list = poInstockService.lambdaQuery()
                .in(PoInstockEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(PoInstockEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(PoInstockEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的采购入库单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeStockInService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送直接调拨单
     */
    @XxlJob("kingdeePushTransferInfo")
    public void kingdeePushTransferInfo() {
        List<TransferInfoEntity> list = transferInfoService.lambdaQuery()
                .in(TransferInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(TransferInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(TransferInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的直接调拨单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeTransferInfoService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("单据【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("单据【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送仓库
     */
    @XxlJob("kingdeePushWarehouse")
    public void kingdeePushWarehouse() {
        List<WarehouseEntity> list = warehouseService.lambdaQuery()
                .in(WarehouseEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(WarehouseEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(WarehouseEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的仓库");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeWarehouseService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("仓库【{}】推送金蝶失败,error = {}",obj.getName(),e);
                log.error("仓库【{}】推送金蝶失败",obj.getName(),e);
            }
        });
    }

    /**
     * 推送销售退货入库单
     */
    @XxlJob("kingdeePushSoReturn")
    public void kingdeePushSoReturn() {
        List<SoReturnInstockEntity> list = soReturnInstockService.lambdaQuery()
                .in(SoReturnInstockEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SoReturnInstockEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SoReturnInstockEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售退货单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSoReturnService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("销售退货单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("销售退货单【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送采购收货单
     */
    @XxlJob("kingdeePoReceive")
    public void kingdeePoReceive() {
        List<WarehouseReceiveEntity> list = warehouseReceiveService.lambdaQuery()
                .in(WarehouseReceiveEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(WarehouseReceiveEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(WarehouseReceiveEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售退货单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeePoReceiveService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("销售退货单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("销售退货单【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }
}
