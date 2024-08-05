package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.threadlocal.UserContext;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.server.wms.mapper.VirtualWarehousePushHandleDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

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
    private DmpMqFeign dmpMqFeign;
    private String splitStr = "_&_";

    @GlobalTransactional(rollbackFor = Exception.class)
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单合单明细单", virtualWarehousePushHandleDetailEntity.getCode());
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "分货单合单明细单"));
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
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehousePushHandleDetailEntity.getCode(), "分货单合单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehousePushHandleDetailEntity, null, virtualWarehousePushHandleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 保存处理明细
     *
     * @param allocationEntity
     * @param pushHandleEntity
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
//    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleDetail(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity) {
        //获取所有明细
        List<VirtualWarehouseAllocationDetailEntity> vmAllocationDetailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        List<VirtualWarehousePushHandleDetailEntity> handleDetailList = new ArrayList<>();
        List<VirtualWarehouseAllocationDetailEntity> noSyncDetailList = new ArrayList<>();
        String type = allocationEntity.getType();
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                saveToHandleDetail(vmAllocationDetailList, allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList);
                break;
            case CANCEL:
                saveFromHandleDetail(vmAllocationDetailList, allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList);
                break;
            case TRANSFER:
                List<String> fromToIds = new ArrayList<>();
                for (VirtualWarehouseAllocationDetailEntity detailDto : vmAllocationDetailList) {
                    fromToIds.add(detailDto.getFromVirtualWarehouseId());
                    fromToIds.add(detailDto.getToVirtualWarehouseId());
                }
                List<ThirdMappingEntity> fromToThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromToIds);
                Map<String, List<ThirdMappingEntity>> fromToThirdMappingMap = fromToThirdMappingList.stream().collect(Collectors.groupingBy(ThirdMappingEntity::getSysId));

                List<VirtualWarehouseAllocationDetailEntity> fromToVwResultList = new ArrayList<>();
                List<VirtualWarehouseAllocationDetailEntity> fromVwResultList = new ArrayList<>();
                List<VirtualWarehouseAllocationDetailEntity> toVwResultList = new ArrayList<>();
                //两种情况
                vmAllocationDetailList.forEach(vmAllocationDetail -> {
                    //获取调出仓 调入仓关联的第三方仓（旺店通）
                    if (CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getFromVirtualWarehouseId()))
                            && CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getToVirtualWarehouseId()))) {
                        fromToVwResultList.add(vmAllocationDetail);
                    } else if (CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getFromVirtualWarehouseId()))
                            && CollectionUtils.isEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getToVirtualWarehouseId()))) {
                        fromVwResultList.add(vmAllocationDetail);
                    } else if (CollectionUtils.isEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getFromVirtualWarehouseId()))
                            && CollectionUtils.isNotEmpty(fromToThirdMappingMap.get(vmAllocationDetail.getToVirtualWarehouseId()))) {
                        toVwResultList.add(vmAllocationDetail);
                    } else {
                        noSyncDetailList.add(vmAllocationDetail);
                    }
                });
                if (CollectionUtils.isNotEmpty(fromToVwResultList)) {
                    Map<String, List<VirtualWarehouseAllocationDetailEntity>> fromToGroupMap = fromToVwResultList.stream().collect(Collectors.groupingBy(detail -> detail.getFromVirtualWarehouseId() + splitStr + detail.getToVirtualWarehouseId()));
                    fromToGroupMap.forEach((fromToGroupId, groupList) -> {
                        String[] split = fromToGroupId.split(splitStr);
                        List<ThirdMappingEntity> fromMappingList = fromToThirdMappingMap.get(split[0]);
                        List<ThirdMappingEntity> toMappingList = fromToThirdMappingMap.get(split[1]);
                        //获取调出仓绑定的旺店通虚拟仓
                        if (CollectionUtils.isNotEmpty(toMappingList)) {
                            //保存合单明细
                            VirtualWarehousePushHandleDetailEntity handleDetailEntity = getHandleDetailEntity(allocationEntity);
                            handleDetailEntity.setFromVirtualWarehouseId(split[0]);
                            handleDetailEntity.setToVirtualWarehouseId(split[1]);
                            handleDetailEntity.setThirdFromVirtualWarehouseId(fromMappingList.get(0).getThirdId());
                            handleDetailEntity.setThirdFromVirtualWarehouseNo(fromMappingList.get(0).getThirdCode());
                            handleDetailEntity.setThirdToVirtualWarehouseId(toMappingList.get(0).getThirdId());
                            handleDetailEntity.setThirdToVirtualWarehouseNo(toMappingList.get(0).getThirdCode());
                            handleDetailEntity.setSysType(fromMappingList.get(0).getThirdSysType());
                            handleDetailEntity.setMainId(pushHandleEntity.getId());
                            handleDetailEntity.setWarehouseId(groupList.get(0).getWarehouseId());
                            handleDetailEntity.setThirdWarehouseId(toMappingList.get(0).getRemark());
                            Integer sumQty = groupList.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum);
                            handleDetailEntity.setQty(sumQty);
                            this.save(handleDetailEntity);
                            handleDetailList.add(handleDetailEntity);
                            groupList.forEach(allocationDetail -> {
                                VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, pushHandleEntity, allocationDetail, handleDetailEntity);
                                virtualWarehousePushHandleRelationService.save(vmAllocationHandleRelationEntity);
                            });
                        }
                    });
                }
                if (CollectionUtils.isNotEmpty(fromVwResultList)) {
                    saveFromHandleDetail(fromVwResultList, allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList);
                }
                if (CollectionUtils.isNotEmpty(toVwResultList)) {
                    saveToHandleDetail(toVwResultList, allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList);
                }
                break;
        }
        if (CollectionUtils.isNotEmpty(handleDetailList)) {
            //推送中台任务:保存任务+发送mq
            List<DmpPushTaskEntity> dmpPushTaskEntityList = syncWdtVirtualWarehousePushOrderService.saveTaskList(handleDetailList,
                    allocationEntity.getCode(), SyncOperateEnum.OPERATE_APPROVE.getCode(), SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode());
            if (CollectionUtils.isNotEmpty(dmpPushTaskEntityList)) {
//                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                    @Override
//                    public void afterCommit() {
                //获取合单表和分货单明细关联关系，修改为同步中状态
                List<String> handleDetailIds = dmpPushTaskEntityList.stream().map(DmpPushTaskEntity::getSourceId).collect(Collectors.toList());
                List<String> allocationDetailList = virtualWarehousePushHandleRelationService.list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                                .in(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetailIds))
                        .stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).distinct().collect(Collectors.toList());
                virtualWarehouseAllocationDetailService.update(new LambdaUpdateWrapper<VirtualWarehouseAllocationDetailEntity>()
                        .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus, VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode()).in(VirtualWarehouseAllocationDetailEntity::getId, allocationDetailList));
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        //发送mq
                        Boolean result = dmpMqFeign.sendTask(dmpPushTaskEntityList);
                        if (Objects.isNull(result) || !result) {
                            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                        }
                    }
                });
            }
//                });
//            }
            //设置没有关联虚拟仓的分货单子单无需同步
            if (CollectionUtils.isNotEmpty(noSyncDetailList)) {
                //设置分货单子单无需同步
                virtualWarehouseAllocationDetailService.update(new LambdaUpdateWrapper<VirtualWarehouseAllocationDetailEntity>()
                        .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus, VirtualWarehouseAllocationSyncStatusEnum.NO_NEED_SYNC.getCode())
                        .in(VirtualWarehouseAllocationDetailEntity::getId, noSyncDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).collect(Collectors.toList())));
            }
        } else {
            //删除合单表主单
            virtualWarehousePushHandleService.forceDeleteById(pushHandleEntity.getId());
            //设置分货单子单无需同步
            virtualWarehouseAllocationDetailService.update(new LambdaUpdateWrapper<VirtualWarehouseAllocationDetailEntity>()
                    .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus, VirtualWarehouseAllocationSyncStatusEnum.NO_NEED_SYNC.getCode())
                    .in(VirtualWarehouseAllocationDetailEntity::getId, vmAllocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).collect(Collectors.toList())));
        }
    }

    /**
     * 保存调出仓
     *
     * @param fromVmList
     * @param allocationEntity
     * @param pushHandleEntity
     * @param handleDetailList
     * @param noSyncDetailList
     */
    private void saveFromHandleDetail(List<VirtualWarehouseAllocationDetailEntity> fromVmList, VirtualWarehouseAllocationEntity allocationEntity,
                                      VirtualWarehousePushHandleEntity pushHandleEntity, List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<VirtualWarehouseAllocationDetailEntity> noSyncDetailList) {
        Map<String, List<VirtualWarehouseAllocationDetailEntity>> cancelMap = fromVmList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId));
        List<String> fromVwId = fromVmList.stream().map(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId).collect(Collectors.toList());
        List<ThirdMappingEntity> fromThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromVwId);
        saveList(allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList, cancelMap, fromThirdMappingList, VwAllocationDirectionEnum.REVERSE);
    }

    public void saveList(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity,
                         List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<VirtualWarehouseAllocationDetailEntity> noSyncDetailList,
                         Map<String, List<VirtualWarehouseAllocationDetailEntity>> cancelMap,
                         List<ThirdMappingEntity> fromThirdMappingList, VwAllocationDirectionEnum code) {
        cancelMap.forEach((fromVmId, allocationDetailList) -> {
            //获取调出仓绑定的旺店通虚拟仓
            if (CollectionUtils.isNotEmpty(fromThirdMappingList)) {
                ThirdMappingEntity thirdMapping = fromThirdMappingList.stream().filter(item -> Objects.equals(item.getSysId(), fromVmId)).findFirst().orElse(null);
                if (Objects.nonNull(thirdMapping)) {
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
                    handleDetailEntity.setWarehouseId(allocationDetailList.get(0).getWarehouseId());
                    handleDetailEntity.setThirdWarehouseId(thirdMapping.getRemark());
                    Integer sumQty = allocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum);
                    handleDetailEntity.setQty(sumQty);
                    this.save(handleDetailEntity);
                    handleDetailList.add(handleDetailEntity);
                    allocationDetailList.forEach(allocationDetail -> {
                        VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, pushHandleEntity, allocationDetail, handleDetailEntity);
                        virtualWarehousePushHandleRelationService.save(vmAllocationHandleRelationEntity);
                    });
                } else {
                    noSyncDetailList.addAll(allocationDetailList);
                }
            } else {
                noSyncDetailList.addAll(allocationDetailList);
            }
        });
    }

    /**
     * 保存调入仓
     *
     * @param toVwResultList
     * @param allocationEntity
     * @param pushHandleEntity
     * @param handleDetailList
     * @param noSyncDetailList
     */
    private void saveToHandleDetail(List<VirtualWarehouseAllocationDetailEntity> toVwResultList, VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity, List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<VirtualWarehouseAllocationDetailEntity> noSyncDetailList) {
        Map<String, List<VirtualWarehouseAllocationDetailEntity>> toVmMap = toVwResultList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId));
        List<String> toVwIds = toVwResultList.stream().map(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId).collect(Collectors.toList());
        List<ThirdMappingEntity> toMappingList = dmpThirdMappingFeign.getVwListBySysIds(toVwIds);
        saveList(allocationEntity, pushHandleEntity, handleDetailList, noSyncDetailList, toVmMap, toMappingList, VwAllocationDirectionEnum.FORWARD);
    }

    private void saveToList(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehousePushHandleEntity pushHandleEntity,
                            List<VirtualWarehousePushHandleDetailEntity> handleDetailList, List<VirtualWarehouseAllocationDetailEntity> noSyncDetailList,
                            Map<String, List<VirtualWarehouseAllocationDetailEntity>> toVmMap,
                            List<ThirdMappingEntity> toMappingList) {
        toVmMap.forEach((toVmId, allocationDetailList) -> {
            //获取调出仓绑定的旺店通虚拟仓
            if (CollectionUtils.isNotEmpty(toMappingList)) {
//                toMappingList.forEach(thirdMapping -> {
                ThirdMappingEntity thirdMapping = toMappingList.stream().filter(item -> Objects.equals(item.getSysId(), toVmId)).findFirst().orElse(null);
                if (Objects.nonNull(thirdMapping)) {
                    //保存合单明细
                    VirtualWarehousePushHandleDetailEntity handleDetailEntity = getHandleDetailEntity(allocationEntity);

                    handleDetailEntity.setToVirtualWarehouseId(toVmId);
                    handleDetailEntity.setThirdToVirtualWarehouseId(thirdMapping.getThirdId());
                    handleDetailEntity.setThirdToVirtualWarehouseNo(thirdMapping.getThirdCode());
                    handleDetailEntity.setThirdFromVirtualWarehouseId("");
                    handleDetailEntity.setThirdFromVirtualWarehouseNo("");
                    handleDetailEntity.setSysType(thirdMapping.getThirdSysType());
                    handleDetailEntity.setMainId(pushHandleEntity.getId());
                    handleDetailEntity.setWarehouseId(allocationDetailList.get(0).getWarehouseId());
                    handleDetailEntity.setThirdWarehouseId(thirdMapping.getRemark());
                    Integer sumQty = allocationDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum);
                    handleDetailEntity.setQty(sumQty);
                    this.save(handleDetailEntity);
                    handleDetailList.add(handleDetailEntity);
                    allocationDetailList.forEach(allocationDetail -> {
                        VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, pushHandleEntity, allocationDetail, handleDetailEntity);
                        virtualWarehousePushHandleRelationService.save(vmAllocationHandleRelationEntity);
                    });
                } else {
                    noSyncDetailList.addAll(allocationDetailList);
                }
//                });
            } else {
                noSyncDetailList.addAll(allocationDetailList);
            }
        });
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
