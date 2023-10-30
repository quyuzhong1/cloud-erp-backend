package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.entity.*;
import com.erp.server.scm.kingdee.*;
import com.erp.server.scm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/10/30 11:51
 */
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private SyncKingdeePurchaseOrderService syncKingdeePurchaseOrderService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private SyncKingdeePurchaseChangeService syncKingdeePurchaseChangeService;

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Resource
    private SyncKingdeeSubcontractOrderService syncKingdeeSubcontractOrderService;

    @Resource
    private SubcontractChangeService subcontractChangeService;

    @Resource
    private SyncKingdeeSubcontractChangeService syncKingdeeSubcontractChangeService;

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private SyncKingdeePurchasePriceChangeService syncKingdeePurchasePriceChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private SyncKingdeeSupplierService syncKingdeeSupplierService;

    @Override
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();

        switch (sourceType) {
            case PURCHASE_ORDER:
                syncPurchaseOrder(sourceDetailList);
                return;
            case PURCHASE_CHANGE:
                syncPurchaseChange(sourceDetailList);
                return;
            case PURCHASE_PRICE:
                syncPurchasePrice(sourceDetailList);
                return;
            case PURCHASE_PRICE_CHANGE:
                syncPurchasePriceChange(sourceDetailList);
                return;
            case SUBCONTRACT_ORDER:
                syncSubcontractOrder(sourceDetailList);
                return;
            case SUBCONTRACT_CHANGE:
                syncSubcontractChange(sourceDetailList);
                return;
            case SUPPLIER:
                syncSupplier(sourceDetailList);
                return;
            default:
                return;
        }
    }

    /**
     * @description: 同步订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncPurchaseOrder (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PurchaseOrderEntity> list = purchaseOrderService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPurchaseOrder >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PurchaseOrderEntity purchaseOrderEntity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                continue;
            }
            syncKingdeePurchaseOrderService.syncDataToKingdee(purchaseOrderEntity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步订单变更
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncPurchaseChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PurchaseChangeEntity> list = purchaseChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPurchaseChange >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PurchaseChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeePurchaseChangeService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步委外订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSubcontractOrder (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractOrderEntity> list = subcontractOrderService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSubcontractOrder >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SubcontractOrderEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSubcontractOrderService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步委外变更单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSubcontractChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractChangeEntity> list = subcontractChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPurchaseOrder >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SubcontractChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSubcontractChangeService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步报价
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncPurchasePrice (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PurchasePriceEntity> list = purchasePriceService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPurchasePrice >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PurchasePriceEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeePurchasePriceService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步订单
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncPurchasePriceChange (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PurchasePriceChangeEntity> list = purchasePriceChangeService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPurchasePriceChange >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PurchasePriceChangeEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeePurchasePriceChangeService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * @description: 同步供应商
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSupplier (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SupplierEntity> list = supplierService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSupplier >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SupplierEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSupplierService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

}
