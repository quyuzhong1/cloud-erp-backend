package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @GlobalTransactional(rollbackFor = Exception.class)
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单明细", virtualWarehouseAllocationDetailEntity.getId());
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "分货单明细"));
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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationDetailEntity.getId(), "分货单明细");
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
            throw new ServiceException(ApiError.ERROR_VMALLOCATION_DETAIL_ADD);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String mainId) {
        if (CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            throw new ServiceException(ApiError.ERROR_1040, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getName());
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO manualFinish(VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity, VirtualWarehouseAllocationEntity vwAllocationEntity,
                                       VirtualWarehouseAllocationDTO.ManualFinishDto dto) {
        String existStatus = vwAllocationDetailEntity.getSyncStatus();
        String code = VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode();
        if (Objects.equals(existStatus, code)) {
            throw new ServiceException("存在相同的状态");
        }
        dto.setSysTypeName(PlatformDictEnum.getByCode(dto.getSysType()).getName());
        //根据分货单主单和明细获取分货单合单数据
        //获取合单表明细id
        VirtualWarehousePushHandleRelationEntity handleRelation = virtualWarehousePushHandleRelationService.getOne(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vwAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vwAllocationDetailEntity.getId()));
        if (Objects.nonNull(handleRelation)) {
            //根据合单明细id获取拆单信息
            String handleDetailId = handleRelation.getHandleDetailId();
            List<VirtualWarehousePushHandleRelationEntity> handleRelationEntityList = virtualWarehousePushHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>().eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetailId));
            if (CollectionUtils.isNotEmpty(handleRelationEntityList)) {
                baseMapper.batchManualFinish(dto, Integer.valueOf(code), handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList()));
            }
            //手动完结中台任务
            dmpMqFeign.batchNoNeedSyncBySourceId(Collections.singletonList(handleRelation.getHandleDetailId()));
        } else {
            throw new ServiceException(ApiError.ERROR_NO_SYNC);
        }
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
                    throw new ServiceException(ApiError.ERROR_400);
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

    @Override
    public void updateByMainId(String mainId, String syncStatus) {
        this.update(new LambdaUpdateWrapper<VirtualWarehouseAllocationDetailEntity>().eq(VirtualWarehouseAllocationDetailEntity::getMainId, mainId)
                .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus, syncStatus));
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO sync(VirtualWarehouseAllocationDetailEntity vwAllocationDetailEntity, VirtualWarehouseAllocationEntity vwAllocationEntity) {
        //获取合单表明细id
        VirtualWarehousePushHandleRelationEntity handleRelation = virtualWarehousePushHandleRelationService.getOne(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vwAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vwAllocationDetailEntity.getId()));
        if (Objects.nonNull(handleRelation)) {
            //根据合单明细id获取拆单信息
            String handleDetailId = handleRelation.getHandleDetailId();
            List<VirtualWarehousePushHandleRelationEntity> handleRelationEntityList = virtualWarehousePushHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>().eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetailId));
            if (CollectionUtils.isNotEmpty(handleRelationEntityList)) {
                baseMapper.batchSync(VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode(),
                        handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList()));
                //修改中台任务状态并触发mq
                List<String> sourceIds = handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getHandleDetailId).collect(Collectors.toList());
                dmpMqFeign.batchSyncBySourceId(sourceIds);
            }
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
    public DmpPushTaskEntity viewSyncInfo(String id) {
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
        VirtualWarehousePushHandleRelationEntity handleRelation = virtualWarehousePushHandleRelationService.getOne(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vmAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vmAllocationDetailEntity.getId()));
        DmpPushTaskEntity productBomHistoryTask = new DmpPushTaskEntity();
        if (Objects.nonNull(handleRelation)) {
            productBomHistoryTask = dmpMqFeign.getByParam(new DmpSyncTaskDTO.OneDTO(SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),
                    handleRelation.getHandleDetailId(), PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc()));
        }
        return productBomHistoryTask;
    }

    private void handleData(List<VirtualWarehouseAllocationDetailEntity> detailEntityList, String mainId) {
        detailEntityList.forEach(detail -> detail.setMainId(mainId));
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<VirtualWarehouseAllocationDTO.DetailDto> newList, List<VirtualWarehouseAllocationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
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
    public VirtualWarehouseAllocationDTO.ThirdCodeDto view(String id) {
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
        //获取合单表明细id
        VirtualWarehousePushHandleRelationEntity handleRelation = virtualWarehousePushHandleRelationService.getOne(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, vwAllocationEntity.getId())
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceDetailId, vwAllocationDetailEntity.getId()));
        VirtualWarehouseAllocationDTO.ThirdCodeDto thirdCodeDto = new VirtualWarehouseAllocationDTO.ThirdCodeDto();
        if (Objects.nonNull(handleRelation)) {
            //根据合单明细id获取拆单信息
            String handleDetailId = handleRelation.getHandleDetailId();
            List<VirtualWarehousePushHandleRelationEntity> handleRelationEntityList = virtualWarehousePushHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>().eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetailId));
            if (CollectionUtils.isNotEmpty(handleRelationEntityList)) {
                //获取明细
                List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                        .in(VirtualWarehouseAllocationDetailEntity::getId, handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList())));
                thirdCodeDto.setType(vwAllocationEntity.getType());
                thirdCodeDto.setSysType(detailList.get(0).getSysType());
                thirdCodeDto.setSysTypeName(detailList.get(0).getSysTypeName());
                thirdCodeDto.setThirdCode(detailList.get(0).getThirdCode());
                List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailList);
                //获取所有的sku信息
                List<String> skuIds = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
                detailDtos.forEach(detailDto -> {
                    SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())).findFirst().orElse(null);
                    if (Objects.nonNull(skuVO)) {
                        detailDto.setProductName(skuVO.getSkuName());
                        detailDto.setImageUrl(skuVO.getSkuImagesUrl());
                    }
                });
                thirdCodeDto.setDetailList(detailDtos);
            }
        }
        return thirdCodeDto;
    }

    @Override
    public void updateSyncStatus(VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        log.info("旺店通虚拟仓订单创建：批量修改分货单明细同步状态：{}", dto);
        //根据合单明细id获取拆单信息
        List<VirtualWarehousePushHandleRelationEntity> handleRelationEntityList = virtualWarehousePushHandleRelationService
                .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>().eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, dto.getHandelDetailId()));
        if (CollectionUtils.isNotEmpty(handleRelationEntityList)) {
            baseMapper.updateSyncStatus(dto, handleRelationEntityList.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList()));
            log.info("旺店通虚拟仓订单创建：批量修改分货单明细同步状态成功：{}", dto);
        }
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
    /**
     * 初始化第三方编码存在异常的数据
     */
    @Override
    public void initFailThirdCode(String errorMsg) {
        if (StringUtils.isBlank(errorMsg)){
            errorMsg = "check_fail";
        }
        List<VirtualWarehouseAllocationDetailEntity> list = this.lambdaQuery().like(VirtualWarehouseAllocationDetailEntity::getThirdCode, errorMsg).list();
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(this::updateDetailData);
    }

    /**
     * 更新历史数据
     * @param detailEntity
     */
    private void updateDetailData(VirtualWarehouseAllocationDetailEntity detailEntity) {
        String thirdCode = "";
        String finishDescription = detailEntity.getThirdCode();
        String syncStatus = VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode();
        // 正则表达式匹配模式
        String pattern = "\\bVO\\d{12}\\b";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 创建 Matcher 对象
        Matcher m = r.matcher(detailEntity.getThirdCode());
        // 查找匹配的
        if (m.find()) {
            thirdCode = m.group(0);
        }
        this.lambdaUpdate()
                .set(VirtualWarehouseAllocationDetailEntity::getThirdCode,thirdCode)
                .set(VirtualWarehouseAllocationDetailEntity::getFinishDescription,finishDescription)
                .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus,syncStatus)
                .eq(VirtualWarehouseAllocationDetailEntity::getId, detailEntity.getId())
                .update();
    }
}
