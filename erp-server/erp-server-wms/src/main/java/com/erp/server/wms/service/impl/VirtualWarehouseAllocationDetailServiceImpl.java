package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟仓分货单明细 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationDetailServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationDetailMapper, VirtualWarehouseAllocationDetailEntity> implements VirtualWarehouseAllocationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    private VirtualWarehousePushHandleDetailService virtualWarehousePushHandleDetailService;
    @Autowired
    private VirtualWarehousePushHandleRelationServiceImpl virtualWarehousePushHandleRelationServiceImpl;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = new VirtualWarehouseAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationDetailEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);

        log.info("开始新增分货单明细");
        boolean save = super.save(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("分货单明细保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单明细", virtualWarehouseAllocationDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationDetailEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationDetailEntity.getId(), virtualWarehouseAllocationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationDetailEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "分货单明细");
        }
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = BeanMapperUtils.map(VirtualWarehouseAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);
        log.info("编辑 开始修改分货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("分货单明细保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录分货单明细日志数据，id：【{}】", virtualWarehouseAllocationDetailEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationDetailEntity.getId(), "分货单明细");
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationDetailEntity, null, virtualWarehouseAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String mainId) {
        List<VirtualWarehouseAllocationDetailEntity> detailEntityList = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, addDTO.getDetailList());
        handleData(detailEntityList, mainId);
        boolean save = super.saveBatch(detailEntityList);
        if (!save) {
            throw new ServiceException(ApiError.VM_ALLOCATION_DETAIL_SAVE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String mainId) {
        if (CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            throw new ServiceException(ApiError.BILL_DETAIL_NOT_FOUND, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getName());
        }
        List<VirtualWarehouseAllocationDetailEntity> oldList = this.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>().eq(VirtualWarehouseAllocationDetailEntity::getMainId, mainId));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }

        List<VirtualWarehouseAllocationDetailEntity> list = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, updateDTO.getDetailList());
        // 数据处理
        handleData(list, mainId);
        //添加操作日志
        List<String> addList = list.stream().map(VirtualWarehouseAllocationDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<VirtualWarehouseAllocationDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个分货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    /**
     * 手动完结
     *
     * @param vwAllocationDetailEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO manualFinish(VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity, VirtualWarehouseAllocationEntity vwAllocationEntity,
                                       VirtualWarehouseAllocationDTO.ManualFinishDto dto) {

        List<VirtualWarehousePushHandleRelationEntity> relationList = virtualWarehousePushHandleRelationService.listBySourceIds(Arrays.asList(vwAllocationEntity.getId()), Arrays.asList(vwAllocationDetailEntity.getId()));
        if (CollUtil.isEmpty(relationList)) {
            log.error("未找到关联关系表数据，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
           return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_NO_SYNC_INFO.getMsg());
        }
        List<String> handleDetailIdList = relationList.stream().map(VirtualWarehousePushHandleRelationEntity::getHandleDetailId).distinct().collect(Collectors.toList());
        List<VirtualWarehousePushHandleDetailEntity> hanleDetailList = virtualWarehousePushHandleDetailService.listByIds(handleDetailIdList);
        if (CollUtil.isEmpty(hanleDetailList)) {
            log.error("未找到推送明细表数据，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_NO_SYNC_INFO.getMsg());
        }
        //同步状态
        List<String> syncStatusList = hanleDetailList.stream().map(VirtualWarehousePushHandleDetailEntity::getSyncStatus).distinct().collect(Collectors.toList());
        if (syncStatusList.size() > 1 || (syncStatusList.size() == 1 && !syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode()))) {
            log.error("分货单明细手动完结状态异常，分货单id：{}，分货单明细id：{}，同步状态列表：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId(), syncStatusList);
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_MANUAL_STATUS_ERROR.getMsg());
        }

        String code = VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode();
        if (syncStatusList.contains(code)) {
            throw new ServiceException("存在相同的状态");
        }
        dto.setSysTypeName(PlatformDictEnum.getByCode(dto.getSysType()).getName());

        //批量手动完结
        virtualWarehousePushHandleDetailService.batchManualFinish(dto,code, handleDetailIdList);

        //手动完结中台任务
        dmpMqFeign.batchNoNeedSyncBySourceId(handleDetailIdList);

        return BatchResultDTO.success(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), OperationTypeEnum.MANUAL_FINISH);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(VirtualWarehouseAllocationEntity allocationEntity) {
        //查找所有明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>().eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        if (CollectionUtils.isNotEmpty(detailList)) {
            String type = allocationEntity.getType();
            switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
                case ALLOCATION:
                    VirtualInventoryStockDTO.StockParamDTO allocationDto = getAllocationDto(VirtualInventoryBusinessTypeEnum.IN_USABLE, detailList, allocationEntity);
                    allocationDto.setIsSplitBom(Boolean.FALSE);
                    virtualInventoryTransCoreService.approve(allocationDto);
                    break;
                case TRANSFER:
                    VirtualInventoryStockDTO.TransferParamDTO dto = getTransferDTO(allocationEntity, detailList);
                    virtualInventoryTransCoreService.approve(dto);
                    break;
                case CANCEL:
                    VirtualInventoryStockDTO.StockParamDTO cancelDto = getCancelDto(VirtualInventoryBusinessTypeEnum.OUT_USABLE, detailList, allocationEntity);
                    cancelDto.setIsSplitBom(Boolean.FALSE);
                    virtualInventoryTransCoreService.approve(cancelDto);
                    break;
                default:
                    throw new ServiceException(ApiError.HTTP_BAD_REQUEST);
            }
        }
    }

    private static VirtualInventoryStockDTO.TransferParamDTO getTransferDTO(VirtualWarehouseAllocationEntity allocationEntity, List<VirtualWarehouseAllocationDetailEntity> detailList) {
        VirtualInventoryStockDTO.TransferParamDTO dto = new VirtualInventoryStockDTO.TransferParamDTO();
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_USABLE.getCode());
        List<VirtualInventoryStockDTO.TransferStockDTO> paramList = new ArrayList<>();

        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.TransferStockDTO outInStockDTO = new VirtualInventoryStockDTO.TransferStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setCurWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setTargetWarehouseId(detailDto.getToWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualCurWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualTargetWarehouseId(detailDto.getToVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            paramList.add(outInStockDTO);
        });
        dto.setParamList(paramList);
        return dto;
    }

    private static VirtualInventoryStockDTO.StockParamDTO getAllocationDto(VirtualInventoryBusinessTypeEnum inUsable, List<VirtualWarehouseAllocationDetailEntity> detailList, VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
        allocationDto.setBusinessType(inUsable.getCode());
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getToVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            allocationParamList.add(outInStockDTO);
        });
        allocationDto.setParamList(allocationParamList);
        return allocationDto;
    }

    private static VirtualInventoryStockDTO.StockParamDTO getCancelDto(VirtualInventoryBusinessTypeEnum inUsable, List<VirtualWarehouseAllocationDetailEntity> detailList, VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
        allocationDto.setBusinessType(inUsable.getCode());
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            allocationParamList.add(outInStockDTO);
        });
        allocationDto.setParamList(allocationParamList);
        return allocationDto;
    }

    /**
     * 同步
     *
     * @param vwAllocationDetailEntity
     * @param vwAllocationEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO sync(VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity, VirtualWarehouseAllocationEntity vwAllocationEntity) {

        List<VirtualWarehousePushHandleRelationEntity> relationList = virtualWarehousePushHandleRelationService.listBySourceIds(Arrays.asList(vwAllocationEntity.getId()), Arrays.asList(vwAllocationDetailEntity.getId()));
        if (CollUtil.isEmpty(relationList)) {
            log.warn("未找到关联关系表数据，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_MANUAL_STATUS_ERROR.getMsg());
        }
        List<String> hanleDetailIdList = relationList.stream().map(VirtualWarehousePushHandleRelationEntity::getHandleDetailId).distinct().collect(Collectors.toList());
        List<VirtualWarehousePushHandleDetailEntity> hanleDetailList = virtualWarehousePushHandleDetailService.listByIds(hanleDetailIdList);
        if (CollUtil.isEmpty(hanleDetailList)) {
            log.warn("未找到推送明细表数据，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_MANUAL_STATUS_ERROR.getMsg());
        }
        //同步状态
        List<String> syncStatusList = hanleDetailList.stream().map(VirtualWarehousePushHandleDetailEntity::getSyncStatus).distinct().collect(Collectors.toList());
        if (!syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode())) {
            log.warn("分货单明细手动同步异常，分货单id：{}，分货单明细id：{}，同步状态列表：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId(), syncStatusList);
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), ApiError.VM_MANUAL_STATUS_ERROR.getMsg());
        }
        List<String> thirdCodes = hanleDetailList.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getThirdCode())).map(VirtualWarehousePushHandleDetailEntity::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(thirdCodes)) {
            log.warn("分货单明细手动同步异常，存在同步成功的第三方单号，分货单id：{}，分货单明细id：{}，单号：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId(), thirdCodes);
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(),"已存在未同步成功的第三方单号，无法同步");
        }

        //获取合单表明细id
        List<VirtualWarehousePushHandleRelationEntity> handleRelationList = virtualWarehousePushHandleRelationService.list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vwAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vwAllocationDetailEntity.getId()));
        if (CollUtil.isEmpty(handleRelationList)) {
            return BatchResultDTO.fail(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), "未找到同步信息");
        }
        //根据合单明细id获取拆单信息
        List<String> handleDetailIdList = handleRelationList.stream().map(VirtualWarehousePushHandleRelationEntity::getHandleDetailId).collect(Collectors.toList());
        List<VirtualWarehousePushHandleDetailEntity> handleRelationEntityList = virtualWarehousePushHandleDetailService
                .list(new LambdaQueryWrapper<VirtualWarehousePushHandleDetailEntity>().in(VirtualWarehousePushHandleDetailEntity::getId, handleDetailIdList));
        if (CollectionUtils.isNotEmpty(handleRelationEntityList)) {
            //修改中台任务状态并触发mq
            List<String> sourceIds = handleRelationEntityList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSyncStatus(),VirtualWarehouseAllocationSyncStatusEnum.TO_BE_SYNC.getCode())
                                    || CharSequenceUtil.equals(obj.getSyncStatus(),VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode()))
                    .map(VirtualWarehousePushHandleDetailEntity::getId).collect(Collectors.toList());
            if (CollUtil.isEmpty(sourceIds)) {
                log.warn("分货单明细同步->未找到可同步的中台任务，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
                return BatchResultDTO.success(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), "未找到同步信息");
            }
            virtualWarehousePushHandleDetailService.updateSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode(),
                    sourceIds);
            dmpMqFeign.batchSyncBySourceId(sourceIds);
        }
        return BatchResultDTO.success(vwAllocationDetailEntity.getId(), vwAllocationEntity.getCode(), "操作成功");
    }

    /**
     * 展示分货单同步信息
     *
     * @param id
     * @return
     */
    @Override
    public List<DmpPushTaskDTO.SyncInfoDTO> viewSyncInfo(String id) {
        VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity = virtualWarehouseAllocationDetailService.getById(id);
        VirtualWarehouseAllocationEntity vmAllocationEntity;
        if (Objects.isNull(vmAllocationDetailEntity)) {
            throw new ServiceException("分货单明细不存在");
        } else {
            vmAllocationEntity = virtualWarehouseAllocationService.getById(vmAllocationDetailEntity.getMainId());
            if (Objects.isNull(vmAllocationEntity)) {
                throw new ServiceException("分货单不存在");
            }
        }
        //获取合单表明细id
        List<VirtualWarehousePushHandleRelationEntity> handleRelationList = virtualWarehousePushHandleRelationService.list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vmAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vmAllocationDetailEntity.getId()));
        List<DmpPushTaskDTO.SyncInfoDTO> syncInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(handleRelationList)) {
            List<String> handleDetailIdList = handleRelationList.stream().map(VirtualWarehousePushHandleRelationEntity::getHandleDetailId).distinct().collect(Collectors.toList());

            syncInfoList = dmpInoutTaskFeign.listErrorData(new DmpSyncTaskDTO.ListDTO(SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),
                    handleDetailIdList, PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc()));
        }
        return syncInfoList;
    }

    private void handleData(List<VirtualWarehouseAllocationDetailEntity> detailEntityList, String mainId) {
        detailEntityList.forEach(detail -> detail.setMainId(mainId));
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<VirtualWarehouseAllocationDTO.DetailDto> newList, List<VirtualWarehouseAllocationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(VirtualWarehouseAllocationDTO.DetailDto::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 根据第三方单号获取明细信息
     *
     * @return
     */
    @Override
    public List<VirtualWarehouseAllocationDTO.ThirdCodeDto> view(String id) {
        VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity = this.getById(id);
        VirtualWarehouseAllocationEntity vwAllocationEntity;
        if (Objects.isNull(vwAllocationDetailEntity)) {
            throw new ServiceException("分货单明细不存在");
        } else {
            vwAllocationEntity = virtualWarehouseAllocationService.getById(vwAllocationDetailEntity.getMainId());
            if (Objects.isNull(vwAllocationEntity)) {
                throw new ServiceException("分货单不存在");
            }
        }
        //根据合单明细id获取拆单信息
        List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> thirdDataDTOS = virtualWarehousePushHandleDetailService.listThirdDataByDetailIdList(Collections.singletonList(vwAllocationDetailEntity.getId()));
        if (CollUtil.isEmpty(thirdDataDTOS)) {
            log.warn("分货单明细第三方编码信息不存在，分货单id：{}，分货单明细id：{}", vwAllocationEntity.getId(), vwAllocationDetailEntity.getId());
            return Collections.emptyList();
        }
        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .in(VirtualWarehouseAllocationDetailEntity::getId, thirdDataDTOS.stream().map(VirtualWarehousePushHandleDetailDTO.ThirdDataDTO::getDetailId).distinct().collect(Collectors.toList())));

        //获取sku信息
        List<String> skuIdList = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        List<VirtualWarehouseAllocationDTO.ThirdCodeDto> thirdCodeDtoList = new ArrayList<>();
        for (VirtualWarehousePushHandleDetailDTO.ThirdDataDTO thirdDataDTO : thirdDataDTOS) {
                 VirtualWarehouseAllocationDTO.ThirdCodeDto thirdCodeDto = new VirtualWarehouseAllocationDTO.ThirdCodeDto();
                thirdCodeDto.setType(vwAllocationEntity.getType());
                thirdCodeDto.setSysType(thirdDataDTO.getSysType());
                thirdCodeDto.setSysTypeName(ThirdSysTypeEnum.getNameByCode(thirdCodeDto.getSysType()));
                thirdCodeDto.setThirdCode(thirdDataDTO.getThirdCode());

                List<VirtualWarehouseAllocationDetailEntity> detailEntityList = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), thirdDataDTO.getDetailId())).collect(Collectors.toList());
                if (ObjUtil.isEmpty(detailEntityList)) {
                    continue;
                }
                List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailEntityList);
                //获取所有的sku信息
                detailDtos.forEach(detailDto -> {
                    ProductDetailEntity skuVO = skuList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getSkuId())).findFirst().orElse(null);
                    if (Objects.nonNull(skuVO)) {
                        detailDto.setProductName(skuVO.getName());
                        detailDto.setImageUrl(skuVO.getImagesUrl());
                    }
                });
                thirdCodeDto.setDetailList(detailDtos);
                thirdCodeDtoList.add(thirdCodeDto);
        }
        return thirdCodeDtoList;

    }

    @Override
    public void updateThirdData(VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        log.info("旺店通虚拟仓订单创建：批量修改分货单明细同步状态：{}", dto);
        //根据合单明细id获取拆单信息
        virtualWarehousePushHandleDetailService.updateThirdData(dto, dto.getHandelDetailId());
        log.info("旺店通虚拟仓订单创建：批量修改分货单明细同步状态成功：{}", dto);
    }

    /**
     * 获取同步信息
     *
     * @param id
     * @author hyj
     */
    @Override
    public VirtualWarehouseAllocationDTO.ManualFinishViewDTO viewManualFinish(String id) {
        VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity = this.getById(id);
        VirtualWarehouseAllocationEntity vwAllocationEntity;
        if (Objects.isNull(vwAllocationDetailEntity)) {
            throw new ServiceException("分货单明细不存在");
        } else {
            vwAllocationEntity = virtualWarehouseAllocationService.getById(vwAllocationDetailEntity.getMainId());
            if (Objects.isNull(vwAllocationEntity)) {
                throw new ServiceException("分货单不存在");
            }
        }
        VirtualWarehouseAllocationDTO.ManualFinishViewDTO manualFinishViewDTO = new VirtualWarehouseAllocationDTO.ManualFinishViewDTO();
        BeanUtils.copyProperties(vwAllocationDetailEntity, manualFinishViewDTO);
        return manualFinishViewDTO;
    }

    /**
     * 更新备注
     */
    @Override
    public Boolean updateRemark(VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO) {
        return lambdaUpdate()
                .set(VirtualWarehouseAllocationDetailEntity::getDetailRemark, updateRemarkDTO.getRemark())
                .eq(VirtualWarehouseAllocationDetailEntity::getId, updateRemarkDTO.getId())
                .update();
    }

    @Override
    public List<VirtualWarehouseAllocationDetailDTO.AllocationDataDTO> listAllocationData(List<String> skuIdList, List<String> warehouseIdList, List<String> virtualWarehouseIdList) {
        return baseMapper.listAllocationData(skuIdList,warehouseIdList,virtualWarehouseIdList);
    }

    @Override
    public List<VirtualWarehouseAllocationDetailEntity> listByMainIdList(List<String> mainIdList) {
        if(CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return  lambdaQuery().in(VirtualWarehouseAllocationDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public List<VirtualWarehouseAllocationDetailEntity> listRepeatHandleDetail(List<String> fromWarehouseIdList, List<String> fromVirtualWarehouseIdList, List<String> skuIdList, List<String> detailIdList) {
        if (CollUtil.isEmpty(fromWarehouseIdList) || CollUtil.isEmpty(fromVirtualWarehouseIdList)
                || CollUtil.isEmpty(skuIdList) || CollUtil.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listRepeatHandleDetail(fromWarehouseIdList,fromVirtualWarehouseIdList,skuIdList,detailIdList);
    }

}
