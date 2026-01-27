package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.service.WdtVirtualInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.wms.mapper.VirtualWarehousePushHandleDetailMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
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

/**
 * <p>
 * 分货单合单明细表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@Service
public class VirtualWarehousePushHandleDetailServiceImpl extends SuperServiceImpl<VirtualWarehousePushHandleDetailMapper, VirtualWarehousePushHandleDetailEntity> implements VirtualWarehousePushHandleDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;
    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;
    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;
    @Resource
    private SyncWdtVirtualWarehousePushOrderService syncWdtVirtualWarehousePushOrderService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private WdtVirtualInventoryService wdtVirtualInventoryService;


    @Resource
    private DmpMqFeign dmpMqFeign;
    private String splitStr = "_&_";

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehousePushHandleDetailDTO.AddDTO addDTO) {
        VirtualWarehousePushHandleDetailEntity virtualWarehousePushHandleDetailEntity = new VirtualWarehousePushHandleDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehousePushHandleDetailEntity);

        // 数据处理
        handleData(virtualWarehousePushHandleDetailEntity);

        log.info("开始新增分货单合单明细单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        virtualWarehousePushHandleDetailEntity.setCode(code);
        boolean save = super.save(virtualWarehousePushHandleDetailEntity);
        if (!save) {
            throw new ServiceException("分货单合单明细单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单合单明细单", virtualWarehousePushHandleDetailEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehousePushHandleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehousePushHandleDetailEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehousePushHandleDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehousePushHandleDetailEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "分货单合单明细单");
        }
        VirtualWarehousePushHandleDetailEntity virtualWarehousePushHandleDetailEntity = BeanMapperUtils.map(VirtualWarehousePushHandleDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehousePushHandleDetailEntity);
        log.info("编辑 开始修改分货单合单明细单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehousePushHandleDetailEntity);
        if (!save) {
            throw new ServiceException("分货单合单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录分货单合单明细单日志数据，单号：【{}】", virtualWarehousePushHandleDetailEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehousePushHandleDetailEntity.getCode(), "分货单合单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehousePushHandleDetailEntity, null, virtualWarehousePushHandleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<VirtualWarehousePushHandleDetailEntity> addAllocationDetailPush(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity) {
        List<VirtualWarehouseAllocationDetailEntity> vmAllocationDetailList = virtualWarehouseAllocationDetailService.listByMainIdList(Collections.singletonList(allocationEntity.getId()));
        if (CollUtil.isEmpty(vmAllocationDetailList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"分货单明细");
        }
        List<VirtualWarehousePushHandleDetailEntity> handleDetailList = new ArrayList<>();
        if (VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode().equals(allocationEntity.getType())){
            saveToHandleDetail(vmAllocationDetailList, allocationEntity, pushHandleEntity, handleDetailList,Boolean.FALSE);
        } else if (VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode().equals(allocationEntity.getType())) {

            List<String> toIds = vmAllocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId).distinct().collect(Collectors.toList());
            List<ThirdMappingEntity> fromToThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(toIds);
            Map<String, List<ThirdMappingEntity>> fromToThirdMappingMap = fromToThirdMappingList.stream().collect(Collectors.groupingBy(ThirdMappingEntity::getSysId));

            List<VirtualWarehouseAllocationDetailEntity> toVwResultList = new ArrayList<>();
            //两种情况
            vmAllocationDetailList.forEach(vmAllocationDetail -> {
                if (CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getToVirtualWarehouseId()))) {
                    //新增调入仓虚拟仓分货
                    toVwResultList.add(vmAllocationDetail);
                }
            });
            if (CollectionUtils.isNotEmpty(toVwResultList)) {
                saveToHandleDetail(toVwResultList, allocationEntity, pushHandleEntity, handleDetailList,Boolean.TRUE);
            }
        } else {
            throw new ServiceException("取消分货单不可以调用此方法");
        }
        return handleDetailList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<VirtualWarehousePushHandleDetailEntity> cancelAllocationDetailPush(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity) {
        List<VirtualWarehouseAllocationDetailEntity> vmAllocationDetailList = virtualWarehouseAllocationDetailService.listByMainIdList(Collections.singletonList(allocationEntity.getId()));
        if (CollUtil.isEmpty(vmAllocationDetailList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"分货单明细");
        }
        List<VirtualWarehousePushHandleDetailEntity> handleDetailList = new ArrayList<>();
        if (VirtualWarehouseAllocationTypeEnum.CANCEL.getCode().equals(allocationEntity.getType())){
            saveFromHandleDetail(vmAllocationDetailList, allocationEntity, pushHandleEntity, handleDetailList);
        } else if (VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode().equals(allocationEntity.getType())) {

            List<String> fromIds = vmAllocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId).distinct().collect(Collectors.toList());
            List<ThirdMappingEntity> fromToThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromIds);
            Map<String, List<ThirdMappingEntity>> fromToThirdMappingMap = fromToThirdMappingList.stream().collect(Collectors.groupingBy(ThirdMappingEntity::getSysId));

            List<VirtualWarehouseAllocationDetailEntity> toVwResultList = new ArrayList<>();
            //两种情况
            vmAllocationDetailList.forEach(vmAllocationDetail -> {
                if (CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getFromVirtualWarehouseId()))) {
                    //取消调入仓虚拟仓分货
                    toVwResultList.add(vmAllocationDetail);
                }
            });
            if (CollectionUtils.isNotEmpty(toVwResultList)) {
                saveFromHandleDetail(toVwResultList, allocationEntity, pushHandleEntity, handleDetailList);
            }
        } else {
            throw new ServiceException("新增分货单不可以调用此方法");
        }

        return handleDetailList;
    }

    @Override
    public void updateSyncStatus(String syncStatus, List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        lambdaUpdate().in(VirtualWarehousePushHandleDetailEntity::getId,ids).set(VirtualWarehousePushHandleDetailEntity::getSyncStatus,syncStatus).update();
    }

    @Override
    public void updateThirdData(VirtualWarehouseAllocationDTO.SyncUpdateDto dto, String handelDetailId) {
        baseMapper.updateThirdData(dto, handelDetailId);
    }

    @Override
    public List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> listThirdDataByDetailIdList(List<String> detailIdList) {
        if (CollUtil.isEmpty(detailIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listThirdDataByDetailIdList(detailIdList);
    }

    @Override
    public void batchManualFinish(VirtualWarehouseAllocationDTO.ManualFinishDto dto, String code, List<String> handleDetailIdList) {
        baseMapper.batchManualFinish(dto,code,handleDetailIdList);
    }




    /**
     * 保存调出仓
     *
     * @param fromVmList
     * @param allocationEntity
     * @param pushHandleEntity
     * @param handleDetailList
     */
    private void saveFromHandleDetail(List<VirtualWarehouseAllocationDetailEntity> fromVmList, VirtualWarehouseAllocationEntity allocationEntity,
                                      VirtualWarehousePushHandleEntity pushHandleEntity, List<VirtualWarehousePushHandleDetailEntity> handleDetailList) {
        Map<String, List<VirtualWarehouseAllocationDetailEntity>> cancelMap = fromVmList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId));
        List<String> fromVwId = fromVmList.stream().map(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId).collect(Collectors.toList());
        List<ThirdMappingEntity> fromThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromVwId);
        saveList(allocationEntity, pushHandleEntity, handleDetailList, cancelMap, fromThirdMappingList, VwAllocationDirectionEnum.REVERSE,Boolean.FALSE);
    }

    /**
     * 新增分货为正向、取消分货为反项,isTransfer调拨标识的取调入仓
     * @author will
     * @date 2025/12/31 15:47
     * @param allocationEntity
     * @param pushHandleEntity
     * @param handleDetailList
     * @param cancelMap
     * @param fromThirdMappingList
     * @param code
     * @param isTransfer
     * @return void
     */
    public void saveList(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity,
                         List<VirtualWarehousePushHandleDetailEntity> handleDetailList,
                         Map<String, List<VirtualWarehouseAllocationDetailEntity>> cancelMap,
                         List<ThirdMappingEntity> fromThirdMappingList, VwAllocationDirectionEnum code,Boolean isTransfer) {
        cancelMap.forEach((fromVmId, allocationDetailList) -> {
            if (CollUtil.isEmpty(fromThirdMappingList)) {
                return;
            }
            //获取调出仓绑定的旺店通虚拟仓
            ThirdMappingEntity thirdMapping = fromThirdMappingList.stream().filter(item -> Objects.equals(item.getSysId(), fromVmId)).findFirst().orElse(null);
            if (ObjUtil.isEmpty(thirdMapping)) {
                return;
            }
            //保存合单明细
            VirtualWarehousePushHandleDetailEntity handleDetailEntity = getHandleDetailEntity(allocationEntity);
            switch (code) {
                case FORWARD:
                    handleDetailEntity.setToVirtualWarehouseId(fromVmId);
                    handleDetailEntity.setThirdToVirtualWarehouseId(thirdMapping.getThirdId());
                    handleDetailEntity.setThirdToVirtualWarehouseNo(thirdMapping.getThirdCode());
                    break;
                default:
                    handleDetailEntity.setFromVirtualWarehouseId(fromVmId);
                    handleDetailEntity.setThirdFromVirtualWarehouseId(thirdMapping.getThirdId());
                    handleDetailEntity.setThirdFromVirtualWarehouseNo(thirdMapping.getThirdCode());
                    break;
            }
            handleDetailEntity.setSysType(thirdMapping.getThirdSysType());
            handleDetailEntity.setMainId(pushHandleEntity.getId());
            String warehouseId = allocationDetailList.get(0).getWarehouseId();
            if (isTransfer){
                warehouseId = allocationDetailList.get(0).getToWarehouseId();
            }
            handleDetailEntity.setWarehouseId(warehouseId);
            handleDetailEntity.setThirdWarehouseId(thirdMapping.getRemark());
            Integer sumQty = allocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum);
            handleDetailEntity.setQty(sumQty);
            handleDetailEntity.setSyncStatus(SyncStatusEnum.TO_BE_SYNC.getCode());
            this.save(handleDetailEntity);
            handleDetailList.add(handleDetailEntity);
            allocationDetailList.forEach(allocationDetail -> {
                VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, pushHandleEntity, allocationDetail, handleDetailEntity);
                virtualWarehousePushHandleRelationService.save(vmAllocationHandleRelationEntity);
            });
        });
    }

    /**
     * 保存调入仓
     *
     * @param toVwResultList
     * @param allocationEntity
     * @param pushHandleEntity
     * @param handleDetailList
     * @param isTransfer
     */
    private void saveToHandleDetail(List<VirtualWarehouseAllocationDetailEntity> toVwResultList, VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity,
                                    List<VirtualWarehousePushHandleDetailEntity> handleDetailList, Boolean isTransfer) {
        Map<String, List<VirtualWarehouseAllocationDetailEntity>> toVmMap = toVwResultList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId));
        List<String> toVwIds = toVwResultList.stream().map(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId).collect(Collectors.toList());
        List<ThirdMappingEntity> toMappingList = dmpThirdMappingFeign.getVwListBySysIds(toVwIds);
        saveList(allocationEntity, pushHandleEntity, handleDetailList, toVmMap, toMappingList, VwAllocationDirectionEnum.FORWARD,isTransfer);
    }



    private static VirtualWarehousePushHandleRelationEntity getHandleRelationEntity(VirtualWarehouseAllocationEntity allocationEntity,
                                                                                    VirtualWarehousePushHandleEntity pushHandleEntity,
                                                                                    VirtualWarehouseAllocationDetailEntity allocationDetail,
                                                                                    VirtualWarehousePushHandleDetailEntity handleDetailEntity) {
        VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = new VirtualWarehousePushHandleRelationEntity();
        vmAllocationHandleRelationEntity.setSourceId(allocationEntity.getId());
        vmAllocationHandleRelationEntity.setSourceDetailId(allocationDetail.getId());
        vmAllocationHandleRelationEntity.setHandleId(pushHandleEntity.getId());
        vmAllocationHandleRelationEntity.setHandleDetailId(handleDetailEntity.getId());
        return vmAllocationHandleRelationEntity;
    }

    private static VirtualWarehousePushHandleDetailEntity getHandleDetailEntity(VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualWarehousePushHandleDetailEntity handleDetailEntity = new VirtualWarehousePushHandleDetailEntity();
        handleDetailEntity.setSourceId(allocationEntity.getId());
        handleDetailEntity.setDirection(allocationEntity.getDirection());
        handleDetailEntity.setType(allocationEntity.getType());
        handleDetailEntity.setStatus(allocationEntity.getStatus());
        return handleDetailEntity;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehousePushHandleDetailEntity virtualWarehousePushHandleDetailEntity) {
    }
}
