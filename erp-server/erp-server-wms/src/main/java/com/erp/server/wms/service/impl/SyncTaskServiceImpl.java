package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.server.wms.kingdee.*;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {
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
    private PoInstockService poInstockService;

    @Resource
    private SyncKingdeeStockInService syncKingdeeStockInService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    @Resource
    private SyncKingdeePoReceiveService syncKingdeePoReceiveService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private SyncKingdeeReturnOrderService syncKingdeeReturnOrderService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SyncKingdeeSoReturnService syncKingdeeSoReturnService;

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    @Resource
    private SyncKingdeeStocktakingLossService syncKingdeeStocktakingLossService;

    @Resource
    private SyncKingdeeStocktakingProfitService syncKingdeeStocktakingProfitService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private SyncKingdeeTransferInfoService syncKingdeeTransferInfoService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SyncKingdeeWarehouseService syncKingdeeWarehouseService;

    @Resource
    private SubcontractIssueService subcontractIssueService;

    @Resource
    private SyncKingdeeSubcontractIssueService syncKingdeeSubcontractIssueService;

    @Override
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();

        switch (sourceType) {
            case MACHINE_INFO:
                syncMachineInfo(sourceDetailList);
                return;
            case OTHER_OUTSTOCK:
                syncOtherOutstock(sourceDetailList);
                return;
            case OTHER_INSTOCK:
                syncOtherInstock(sourceDetailList);
                return;
            case PO_INSTOCK:
                syncPoInstock(sourceDetailList);
                return;
            case PO_RECEIVE:
                syncPoReceive(sourceDetailList);
                return;
            case PO_RETURN:
                syncPoReturn(sourceDetailList);
                return;
            case SO_OUTSTOCK:
                syncSoOutstock(sourceDetailList);
                return;
            case SO_RETURN_INSTOCK:
                syncSoReturnInstock(sourceDetailList);
                return;
            case STOCKTAKING_PROFIT_LOSS:
                syncStocktakingTaskProfitLoss(sourceDetailList);
                return;
            case TRANSFER_INFO:
                syncTransferInfo(sourceDetailList);
                return;
            case WAREHOUSE:
                syncWarehouse(sourceDetailList);
            case SUBCONTRACT_ISSUE:
                syncSubcontractIssue(sourceDetailList);
                return;
        }
    }

    /**
     * 仓库
     * @param sourceDetailList
     */
    private void syncSubcontractIssue(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractIssueEntity> list = subcontractIssueService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SubcontractIssueEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSubcontractIssueService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 仓库
     * @param sourceDetailList
     */
    private void syncWarehouse(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseEntity> list = warehouseService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            WarehouseEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeWarehouseService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 调拨单
     * @param sourceDetailList
     */
    private void syncTransferInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<TransferInfoEntity> list = transferInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncTransferInfo >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            TransferInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeTransferInfoService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 盘赢盘亏单
     * @param sourceDetailList
     */
    private void syncStocktakingTaskProfitLoss(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<StocktakingProfitLossEntity> list = stocktakingProfitLossService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncStocktakingTaskProfitLoss >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            StocktakingProfitLossEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            if (entity.getBillType().equals(BillTypeEnum.LOSS)) {
                syncKingdeeStocktakingLossService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
            } else {
                syncKingdeeStocktakingProfitService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
            }
        }
    }

    /**
     * 销售退货入库单
     * @param sourceDetailList
     */
    private void syncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoReturnInstockEntity> list = soReturnInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoReturnInstock >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoReturnInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSoReturnService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 销售出库单
     * @param sourceDetailList
     */
    private void syncSoOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoOutstockEntity> list = soOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoOutstock >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoOutstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeSoOutstockService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
            syncKingdeeSoOutstockService.syncOrderToDmp(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 采购退货
     * @param sourceDetailList
     */
    private void syncPoReturn(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoReturnEntity> list = poReturnService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReturn >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PoReturnEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeReturnOrderService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 采购收货单
     * @param sourceDetailList
     */
    private void syncPoReceive(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseReceiveEntity> list = warehouseReceiveService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReceive >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            WarehouseReceiveEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeePoReceiveService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 采购入库单
     * @param sourceDetailList
     */
    private void syncPoInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoInstockEntity> list = poInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoInstock >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PoInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeStockInService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 其他入库
     * @param sourceDetailList
     */
    private void syncOtherInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherInstockEntity> list = otherInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherInstock >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            OtherInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeOtherInstockService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }


    /**
     * 其他出库单
     * @param sourceDetailList
     */
    private void syncOtherOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherOutstockEntity> list = otherOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherOutstock >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            OtherOutstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeOtherOutstockService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 加工单
     * @param sourceDetailList
     */
    private void syncMachineInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<MachineInfoEntity> list = machineInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncMachineInfo >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            MachineInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            syncKingdeeMachineInfoService.syncDataToKingdee(entity,syncParamDetailDTO.getSyncOperate());
        }
    }



}
