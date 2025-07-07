package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDetailDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.BillTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.*;
import com.erp.server.wms.mabang.SyncMabangMachineService;
import com.erp.server.wms.mabang.SyncMabangTransferService;
import com.erp.server.wms.service.*;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.*;
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
    private TransferInService transferInService;

    @Resource
    private TransferInDetailService transferInDetailService;

    @Resource
    private SyncKingdeeTransferInService syncKingdeeTransferInService;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Resource
    private SyncKingdeeTransferOutService syncKingdeeTransferOutService;

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

    @Resource
    private SyncMabangMachineService syncMabangMachineService;

    @Resource
    private SyncMabangTransferService syncMabangTransferService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private SyncSoReturnInstockService syncSoReturnInstockService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoInfoFeign soInfoFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        switch (sourceType) {
            case MACHINE_INFO:
                resultList = syncMachineInfo(sourceDetailList);
                break;
            case OTHER_OUTSTOCK:
                resultList = syncOtherOutstock(sourceDetailList);
                break;
            case OTHER_INSTOCK:
                resultList = syncOtherInstock(sourceDetailList);
                break;
            case PO_INSTOCK:
                resultList = syncPoInstock(sourceDetailList);
                break;
            case PO_RECEIVE:
                resultList = syncPoReceive(sourceDetailList);
                break;
            case PO_RETURN:
                resultList = syncPoReturn(sourceDetailList);
                break;
            case SO_OUTSTOCK:
                resultList = syncSoOutstock(sourceDetailList);
                break;
            case SO_RETURN_INSTOCK:
                resultList = syncSoReturnInstock(sourceDetailList);
                break;
            case STOCKTAKING_PROFIT_LOSS:
                resultList = syncStocktakingTaskProfitLoss(sourceDetailList);
                break;
            case TRANSFER_INFO:
                resultList = syncTransferInfo(sourceDetailList);
                break;
            case WAREHOUSE:
                resultList = syncWarehouse(sourceDetailList);
                break;
            case SUBCONTRACT_ISSUE:
                resultList = syncSubcontractIssue(sourceDetailList);
                break;
            default:
                break;
        }
        //推送金蝶
        List<DmpPushTaskEntity> finalResultList = resultList;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(finalResultList);
            }
        });
    }

    @Override
    public void findMaBangDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        switch (sourceType) {
            case MACHINE_INFO:
                syncMaBangMachineInfo(sourceDetailList);
                return;
            case TRANSFER_INFO:
                syncMaBangTransferInfo(sourceDetailList);
                return;
        }
    }

    @Override
    public void findWdtDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        if(sourceType == SourceTypeEnum.WDT_OTHER_INSTOCK) {
        	sourceType = SourceTypeEnum.OTHER_INSTOCK;
        	syncParamDTO.setSourceType(sourceType);
        }else if(sourceType == SourceTypeEnum.WDT_OTHER_OUTSTOCK) {
        	sourceType = SourceTypeEnum.OTHER_OUTSTOCK;
        	syncParamDTO.setSourceType(sourceType);
        }
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<DmpPushWdtDTO.ViewDTO> viewDTOList = dmpPushWdtFeign.listByIds(sourceIdList);
        List<String> warehouseIds = viewDTOList.stream().map(item -> item.getWarehouseId()).collect(Collectors.toList());
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(warehouseIds, "wdt");
        Map<String, String> thirdWarehouseMap = mappingList.stream().collect(Collectors.toMap(item1 -> item1.getSysWarehouseId(), item2 -> item2.getThirdWarehouseCode()));
        for (DmpPushWdtDTO.ViewDTO viewDTO : viewDTOList) {
            if(! thirdWarehouseMap.containsKey(viewDTO.getWarehouseId())){
                log.error("没有找到第三方仓库映射: {}", viewDTO);
                continue;
            }
            String thirdWarehouseCode = thirdWarehouseMap.get(viewDTO.getWarehouseId());
            SyncOperateEnum operateEnum = SyncOperateEnum.getByCode(viewDTO.getOperateType());
            BusinessNoTypeEnum businessNoTypeEnum = getBusinessNoType(sourceType);
            List<CommonCreateBillGoodsReq> goodsList = getGoodsList(viewDTO.getDetailDTOList(), viewDTO.getWarehouseId());
            abstractWdtService.structBill(operateEnum, viewDTO.getId(), businessNoTypeEnum, sourceType, goodsList, thirdWarehouseCode);
        }
    }

    private List<CommonCreateBillGoodsReq> getGoodsList(List<DmpPushWdtDetailDTO> detailDTOList, String warehouseId) {
        List<CommonCreateBillGoodsReq> list = new ArrayList<>(detailDTOList.size());
        for (DmpPushWdtDetailDTO detailDTO : detailDTOList) {
            CommonCreateBillGoodsReq request = new CommonCreateBillGoodsReq();
            request.setSpecNo(detailDTO.getSpecNo());
            request.setNum(detailDTO.getNum());
            request.setPositionNo(detailDTO.getPositionNo());
            request.setWarehouseId(warehouseId);
            list.add(request);
        }
        return list;
    }

    private BusinessNoTypeEnum getBusinessNoType(SourceTypeEnum sourceType) {
        if(sourceType.compareTo(SourceTypeEnum.OTHER_INSTOCK) == 0){
            return BusinessNoTypeEnum.CODE_QTRK;
        }
        if(sourceType.compareTo(SourceTypeEnum.OTHER_OUTSTOCK) == 0){
            return BusinessNoTypeEnum.CODE_QTCK;
        }
        return null;
    }

    /**
     * 直接调拨单推送马帮
     */
    private void syncMaBangTransferInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
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
            syncMabangTransferService.syncDataToMabang(entity,syncParamDetailDTO.getSyncOperate());
        }
    }

    /**
     * 加个单推送马版
     */
    private void syncMaBangMachineInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
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
            syncMabangMachineService.syncDataToMabang(entity,syncParamDetailDTO.getSyncOperate());
        }
    }


    /**
     * 仓库
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSubcontractIssue(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractIssueEntity> list = subcontractIssueService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SubcontractIssueEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSubcontractIssueService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 仓库
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncWarehouse(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseEntity> list = warehouseService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            WarehouseEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeWarehouseService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 调拨单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncTransferInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<TransferInfoEntity> list = transferInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncTransferInfo >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<String> ids = list.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
        //直接调拨单明细
        List<TransferInfoDetailEntity> transferInfoDetailEntityList = transferInfoDetailService.listByMainIds(ids);
        Map<String, List<TransferInfoDetailEntity>> transferDetailMap = transferInfoDetailEntityList.stream().collect(Collectors.groupingBy(TransferInfoDetailEntity::getMainId));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();

        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            TransferInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<TransferInfoDetailEntity> transferInfoDetailEntityList1 = transferDetailMap.get(entity.getId());
            transferInfoDetailEntityList1 = CollUtil.isNotEmpty(transferInfoDetailEntityList1) ? transferInfoDetailEntityList1.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
            if (CollUtil.isEmpty(transferInfoDetailEntityList1)){
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeTransferInfoService.syncDataToKingdee(entity, transferInfoDetailEntityList1, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 盘赢盘亏单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncStocktakingTaskProfitLoss(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<StocktakingProfitLossEntity> list = stocktakingProfitLossService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncStocktakingTaskProfitLoss >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            StocktakingProfitLossEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            if (entity.getBillType().equals(BillTypeEnum.LOSS)) {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeStocktakingLossService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
                resultList.add(pushTaskEntity);
            } else {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeStocktakingProfitService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
                resultList.add(pushTaskEntity);
            }
        }
        return resultList;
    }

    /**
     * 销售退货入库单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoReturnInstockEntity> list = soReturnInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoReturnInstock >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoReturnInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoReturnService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 销售出库单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncSoOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoOutstockEntity> list = soOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoOutstock >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SoOutstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            //审核通过发送金蝶
            if (!OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
                DmpPushTaskEntity pushTaskEntity = syncKingdeeSoOutstockService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
                resultList.add(pushTaskEntity);
            } else {
                DmpPushTaskEntity pushTaskEntity;
                if ("qimen".equals(entity.getCreateUserName()) || "wangdiantong".equals(entity.getCreateUserName())){
                    pushTaskEntity = syncKingdeeSoOutstockService.syncWdtDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
                }else {
                    pushTaskEntity = syncKingdeeSoOutstockService.syncB2cDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
                }
                resultList.add(pushTaskEntity);
            }
//            syncKingdeeSoOutstockService.syncOrderToDmp(entity,syncParamDetailDTO.getSyncOperate());
        }
        return resultList;
    }

    /**
     * 采购退货
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncPoReturn(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoReturnEntity> list = poReturnService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReturn >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PoReturnEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeReturnOrderService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 采购收货单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncPoReceive(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseReceiveEntity> list = warehouseReceiveService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReceive >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            WarehouseReceiveEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeePoReceiveService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 采购入库单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncPoInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoInstockEntity> list = poInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoInstock >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            PoInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeStockInService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 其他入库
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncOtherInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherInstockEntity> list = otherInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherInstock >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            OtherInstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOtherInstockService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }


    /**
     * 其他出库单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncOtherOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherOutstockEntity> list = otherOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherOutstock >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            OtherOutstockEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOtherOutstockService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

    /**
     * 加工单
     * @param sourceDetailList
     */
    private List<DmpPushTaskEntity> syncMachineInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<MachineInfoEntity> list = machineInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncMachineInfo >>>> 未找到数据！");
            return Collections.emptyList();
        }
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            MachineInfoEntity entity = list.stream().filter(obj -> obj.getId().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            DmpPushTaskEntity pushTaskEntity = syncKingdeeMachineInfoService.syncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            resultList.add(pushTaskEntity);
        }
        return resultList;
    }

	@Override
	public Map<String, Map<String, Object>> newFindDataSendSyncTask(SyncParamDTO syncParamDTO) {
		List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case MACHINE_INFO:
                resultList = newSyncMachineInfo(sourceDetailList);
                break;
            case OTHER_OUTSTOCK:
                resultList = newSyncOtherOutstock(sourceDetailList);
                break;
            case OTHER_INSTOCK:
                resultList = newSyncOtherInstock(sourceDetailList);
                break;
            case PO_INSTOCK:
                resultList = newSyncPoInstock(sourceDetailList);
                break;
            case PO_RECEIVE:
                resultList = newSyncPoReceive(sourceDetailList);
                break;
            case PO_RETURN:
                resultList = newSyncPoReturn(sourceDetailList);
                break;
            case SO_OUTSTOCK:
                resultList = newSyncSoOutstock(sourceDetailList);
                break;
            case SO_RETURN_INSTOCK:
                resultList = newSyncSoReturnInstock(sourceDetailList);
                break;
            case STOCKTAKING_PROFIT_LOSS:
                resultList = newSyncStocktakingTaskProfitLoss(sourceDetailList);
                break;
            case STOCKTAKING_LOSS:
            	resultList = newSyncStocktakingTaskProfitLoss(sourceDetailList);
            	break;
            case STOCKTAKING_PROFIT:
            	resultList = newSyncStocktakingTaskProfitLoss(sourceDetailList);
            	break;
            case TRANSFER_INFO:
                resultList = newSyncTransferInfo(sourceDetailList);
                break;
            case WAREHOUSE:
                resultList = newSyncWarehouse(sourceDetailList);
                break;
            case SUBCONTRACT_ISSUE:
                resultList = newSyncSubcontractIssue(sourceDetailList);
                break;
            case SDY_WAREHOUSE:
            	resultList = newSdySyncWarehouse(sourceDetailList);
            	break;
            case SDY_SO_OUTSTOCK:
            	resultList = newSdySyncSoOutstock(sourceDetailList);
            	break;
            case SDY_SO_RETURN_INSTOCK:
            	resultList = newSdySyncSoReturnInstock(sourceDetailList);
            	break;
            case TRANSFER_IN:
                resultList = newSyncTransferIn(sourceDetailList);
                break;
            case TRANSFER_OUT:
                resultList = newSyncTransferOut(sourceDetailList);
                break;
            case CAINIAO_SO_RETURN_INSTOCK:
                resultList = newCaiNiaoSyncSoReturnInstock(sourceDetailList);
                break;
            default:
                break;
        }
		return resultList;
	}

    private Map<String , Map<String, Object>> newSyncMachineInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
		Map<String , Map<String, Object>> resultList = new HashMap<>();
		List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<MachineInfoEntity> list = machineInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncMachineInfo >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            MachineInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeMachineInfoService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

	/**
     * 其他出库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncOtherOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherOutstockEntity> list = otherOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherOutstock >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            OtherOutstockEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            Map<String, Object> newSyncDataToKingdee = syncKingdeeOtherOutstockService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            if(newSyncDataToKingdee == null) {
            	continue;
            }
			resultList.put(syncParamDetailDTO.getDataId(), newSyncDataToKingdee);
        }
        return resultList;
    }
    
    /**
     * 其他入库
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncOtherInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<OtherInstockEntity> list = otherInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncOtherInstock >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            OtherInstockEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            Map<String, Object> newSyncDataToKingdee = syncKingdeeOtherInstockService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
            if(newSyncDataToKingdee == null) {
            	continue;
            }
			resultList.put(syncParamDetailDTO.getDataId(), newSyncDataToKingdee);
        }
        return resultList;
    }
    
    /**
     * 采购入库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncPoInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoInstockEntity> list = poInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoInstock >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            PoInstockEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeStockInService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    /**
     * 采购收货单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncPoReceive(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseReceiveEntity> list = warehouseReceiveService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReceive >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            WarehouseReceiveEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeePoReceiveService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    /**
     * 采购退货
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncPoReturn(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PoReturnEntity> list = poReturnService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPoReturn >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            PoReturnEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeReturnOrderService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    /**
     * 销售出库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoOutstockEntity> list = soOutstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoOutstock >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoOutstockEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            //审核通过发送金蝶
            if (!OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            	resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoOutstockService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
            } else {
                if ("qimen".equals(entity.getCreateUserName()) || "wangdiantong".equals(entity.getCreateUserName())){
                	resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoOutstockService.newSyncWdtDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
                }else {
                    resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoOutstockService.newSyncB2cDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
                }
            }
        }
        return resultList;
    }
    
    /**
     * 销售退货入库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoReturnInstockEntity> list = soReturnInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSoReturnInstock >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SoReturnInstockEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSoReturnService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    /**
     * 盘赢盘亏单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncStocktakingTaskProfitLoss(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<StocktakingProfitLossEntity> list = stocktakingProfitLossService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncStocktakingTaskProfitLoss >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            StocktakingProfitLossEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            if (entity.getBillType().equals(BillTypeEnum.LOSS)) {
            	resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeStocktakingLossService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
            } else {
            	resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeStocktakingProfitService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
            }
        }
        return resultList;
    }
    
    /**
     * 调拨单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncTransferInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<TransferInfoEntity> list = transferInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncTransferInfo >>>> 未找到数据！");
            return resultList;
        }
        List<String> ids = list.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
        //直接调拨单明细
        List<TransferInfoDetailEntity> transferInfoDetailEntityList = transferInfoDetailService.listByMainIds(ids);
        Map<String, List<TransferInfoDetailEntity>> transferDetailMap = transferInfoDetailEntityList.stream().collect(Collectors.groupingBy(TransferInfoDetailEntity::getMainId));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            TransferInfoEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<TransferInfoDetailEntity> transferInfoDetailEntityList1 = transferDetailMap.get(entity.getId());
            transferInfoDetailEntityList1 = CollUtil.isNotEmpty(transferInfoDetailEntityList1) ? transferInfoDetailEntityList1.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
            if (CollUtil.isEmpty(transferInfoDetailEntityList1)){
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeTransferInfoService.newSyncDataToKingdee(entity, transferInfoDetailEntityList1,syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * 分步式调入
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncTransferIn(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<TransferInEntity> list = transferInService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncTransferInfo >>>> 未找到数据！");
            return resultList;
        }
        List<String> ids = list.stream().map(TransferInEntity::getId).distinct().collect(Collectors.toList());
        //直接调拨单明细
        List<TransferInDetailEntity> transferInDetailEntityList = transferInDetailService.listByMainIdList(ids);
        Map<String, List<TransferInDetailEntity>> transferDetailMap = transferInDetailEntityList.stream().collect(Collectors.groupingBy(TransferInDetailEntity::getMainId));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            TransferInEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<TransferInDetailEntity> transferInDetailEntityList1 = transferDetailMap.get(entity.getId());
            transferInDetailEntityList1 = CollUtil.isNotEmpty(transferInDetailEntityList1) ? transferInDetailEntityList1.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
            if (CollUtil.isEmpty(transferInDetailEntityList1)){
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeTransferInService.newSyncDataToKingdee(entity, transferInDetailEntityList1,syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * 分步式调出
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncTransferOut(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<TransferOutEntity> list = transferOutService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncTransferInfo >>>> 未找到数据！");
            return resultList;
        }
        List<String> ids = list.stream().map(TransferOutEntity::getId).distinct().collect(Collectors.toList());
        //直接调拨单明细
        List<TransferOutDetailEntity> transferOutDetailEntityList = transferOutDetailService.listByMainIds(ids);
        Map<String, List<TransferOutDetailEntity>> transferDetailMap = transferOutDetailEntityList.stream().collect(Collectors.groupingBy(TransferOutDetailEntity::getMainId));
        //服务sku
        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = CollUtil.isNotEmpty(noInventorySku) ?
                noInventorySku.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList()) : Collections.emptyList();
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            TransferOutEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<TransferOutDetailEntity> transferOutDetailEntityList1 = transferDetailMap.get(entity.getId());
            transferOutDetailEntityList1 = CollUtil.isNotEmpty(transferOutDetailEntityList1) ? transferOutDetailEntityList1.stream().filter(e -> !ignoreInventorySkuIds.contains(e.getSkuId())).collect(Collectors.toList()) : Collections.emptyList();
            if (CollUtil.isEmpty(transferOutDetailEntityList1)){
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeTransferOutService.newSyncDataToKingdee(entity, transferOutDetailEntityList1,syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * 仓库
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncWarehouse(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<WarehouseEntity> list = warehouseService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            WarehouseEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeWarehouseService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }
    
    private Map<String , Map<String, Object>> newSdySyncWarehouse(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
    	List<WarehouseEntity> list = warehouseService.listByIds(sourceIdList);
    	if (CollectionUtils.isEmpty(list)) {
    		log.error("syncSdyWarehouse >>>> 未找到数据！");
    		return resultList;
    	}
    	for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
    		String sourceId = syncParamDetailDTO.getSourceId();
    		WarehouseEntity entity = list.stream().filter(obj -> {
    			return obj.getId().equals(sourceId);
    		}).findFirst().orElse(null);
    		if (ObjectUtils.isEmpty(entity)) {
    			continue;
    		}
    		resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeWarehouseService.newSyncDataToSdy(entity, syncParamDetailDTO.getSyncOperate()));
    	}
    	return resultList;
    }

    /**
     * 仓库
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newSyncSubcontractIssue(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
    	Map<String , Map<String, Object>> resultList = new HashMap<>();
    	List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SubcontractIssueEntity> list = subcontractIssueService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncWarehouse >>>> 未找到数据！");
            return resultList;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
        	String sourceId = syncParamDetailDTO.getSourceId();
            SubcontractIssueEntity entity = list.stream().filter(obj -> {
				return obj.getId().equals(sourceId);
			}).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), syncKingdeeSubcontractIssueService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate()));
        }
        return resultList;
    }

    /**
     * 销售出库单
     * @param sourceDetailList
     * @return
     */
    public Map<String ,Map<String, Object>> newSdySyncSoOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        Map<String, List<SyncParamDetailDTO>> operateMaps = sourceDetailList.stream().collect(Collectors.groupingBy(DmpSyncMqDTO.SyncParamDetailDTO::getSyncOperate));
        for(Map.Entry<String, List<SyncParamDetailDTO>> operateMap : operateMaps.entrySet()) {
        	List<SyncParamDetailDTO> value = operateMap.getValue();
        	List<String> sourceIdList = value.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
            //订单同步数帝云是详情级别同步，所以查询详情
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByIds(sourceIdList);
            if (CollectionUtils.isEmpty(soOutstockDetailEntityList)) {
                log.error("newSdySyncSoOutstock >>>> 未找到数据！");
                continue;
            }
            List<String> outstockIds = soOutstockDetailEntityList.stream().map(SoOutstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<SoOutstockEntity> soOutstockEntities = soOutstockService.listByIds(outstockIds);

            Map<String, Map<String, Object>> syncBatchDataToSdy = syncKingdeeSoOutstockService.syncBatchDataToSdy(soOutstockEntities, soOutstockDetailEntityList, operateMap.getKey() , false, value.get(0).isNewQuerySync());
            for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  value) {
                String sourceId = syncParamDetailDTO.getSourceId();
                SoOutstockDetailEntity soOutstockDetailEntity = soOutstockDetailEntityList.stream().filter(req -> req.getId().equals(sourceId)).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(soOutstockDetailEntity)) {
                    continue;
                }
                SoOutstockEntity entity = soOutstockEntities.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(entity)) {
                    continue;
                }
                Map<String, Object> map = syncBatchDataToSdy.get(sourceId);
                if(map == null) {
                	continue;
                }
                resultList.put(syncParamDetailDTO.getDataId(), map);
            }
        }
        return resultList;
    }

    /**
     * 销售退货入库单
     * @param sourceDetailList
     * @return
     */
    public Map<String ,Map<String, Object>> newSdySyncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        
        Map<String, List<SyncParamDetailDTO>> operateMaps = sourceDetailList.stream().collect(Collectors.groupingBy(DmpSyncMqDTO.SyncParamDetailDTO::getSyncOperate));
        for(Map.Entry<String, List<SyncParamDetailDTO>> operateMap : operateMaps.entrySet()) {
        	List<SyncParamDetailDTO> value = operateMap.getValue();
        	
        	List<String> sourceIdList = value.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
            //订单同步数帝云是详情级别同步，所以查询详情
            List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listByIds(sourceIdList);
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.error("newSdySyncSoReturnInstock >>>> 未找到数据！");
                continue;
            }
            List<String> instockIds = detailEntityList.stream().map(SoReturnInstockDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<SoReturnInstockEntity> list = soReturnInstockService.listByIds(instockIds);

            Map<String, Map<String, Object>> syncBatchDataToSdy = syncSoReturnInstockService.syncBatchDataToSdy(list, detailEntityList, operateMap.getKey() , false, value.get(0).isNewQuerySync());
            
            for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  value) {
                String sourceId = syncParamDetailDTO.getSourceId();
                SoReturnInstockDetailEntity detailEntity = detailEntityList.stream().filter(req -> req.getId().equals(sourceId)).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    continue;
                }
                SoReturnInstockEntity entity = list.stream().filter(req -> req.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(entity)) {
                    continue;
                }
                Map<String, Object> map = syncBatchDataToSdy.get(sourceId);
                if(map == null) {
                	continue;
                }
                resultList.put(syncParamDetailDTO.getDataId(), map);
            }
        }
        
        return resultList;
    }

    /**
     * 菜鸟销售退货入库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> newCaiNiaoSyncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SoReturnInstockEntity> list = soReturnInstockService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("newCaiNiaoSyncSoReturnInstock >>>> 未找到数据！");
            return resultList;
        }
        List<String> ids = list.stream().map(SoReturnInstockEntity::getId).distinct().collect(Collectors.toList());
        //直接调拨单明细
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);
        Map<String, List<SoReturnInstockDetailEntity>> soReturnInstockDetailMap = soReturnInstockDetailEntityList.stream().collect(Collectors.groupingBy(SoReturnInstockDetailEntity::getMainId));

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            SoReturnInstockEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailMap.get(entity.getId());
            if (CollUtil.isEmpty(detailEntityList)){
                continue;
            }
            AliexpressReturnInstockDTO aliexpressReturnInstockDTO = soReturnInstockService.newSyncDataToCaiNiao(entity, detailEntityList,syncParamDetailDTO.getSyncOperate());
            if(Objects.isNull(aliexpressReturnInstockDTO)){
                continue;
            }
            Map<String, Object> dataMap = JSONUtil.parseObj(aliexpressReturnInstockDTO);
            resultList.put(syncParamDetailDTO.getDataId(), dataMap);
        }
        return resultList;
    }
}
