package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.server.wms.mapper.VirtualWarehousePushHandleMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualWarehousePushHandleDetailService;
import com.erp.server.wms.service.VirtualWarehousePushHandleRelationService;
import com.erp.server.wms.service.VirtualWarehousePushHandleService;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 分货单拆单主表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@Service
public class VirtualWarehousePushHandleServiceImpl extends SuperServiceImpl<VirtualWarehousePushHandleMapper, VirtualWarehousePushHandleEntity> implements VirtualWarehousePushHandleService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private VirtualWarehousePushHandleDetailService vmAllocationHandleDetailService;

    @Resource
    private SyncWdtVirtualWarehousePushOrderService syncWdtVirtualWarehousePushOrderService;

    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;



    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehousePushHandleDTO.AddDTO addDTO) {
        VirtualWarehousePushHandleEntity virtualWarehousePushHandleEntity = new VirtualWarehousePushHandleEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehousePushHandleEntity);

        // 数据处理
        handleData(virtualWarehousePushHandleEntity);

        log.info("开始新增分货单拆单主单");
        boolean save = super.save(virtualWarehousePushHandleEntity);
        if(!save) {
            throw new ServiceException("分货单拆单主单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单拆单主单" , virtualWarehousePushHandleEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehousePushHandleEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehousePushHandleEntity.getId(), virtualWarehousePushHandleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehousePushHandleDTO.UpdateDTO updateDTO) {
        VirtualWarehousePushHandleEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "分货单拆单主单");
        }
        VirtualWarehousePushHandleEntity virtualWarehousePushHandleEntity =  BeanMapperUtils.map(VirtualWarehousePushHandleEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehousePushHandleEntity);
        log.info("编辑 开始修改分货单拆单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehousePushHandleEntity);
        if(!save) {
            throw new ServiceException("分货单拆单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录分货单拆单主单日志数据，id：【{}】", virtualWarehousePushHandleEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehousePushHandleEntity.getId(), "分货单拆单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehousePushHandleEntity, null, virtualWarehousePushHandleEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public void forceDeleteById(String id) {
        baseMapper.forceDeleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<VirtualWarehousePushHandleDetailEntity> addAllocationPush(VirtualWarehouseAllocationEntity allocationEntity) {
        //查询是否存在分货单拆单主表
        VirtualWarehousePushHandleEntity pushHandleEntity = getByAllocation(allocationEntity);
        List<VirtualWarehousePushHandleDetailEntity> pushDetailList = vmAllocationHandleDetailService.addAllocationDetailPush(allocationEntity, pushHandleEntity);
        if (CollUtil.isEmpty(pushDetailList)) {
            return pushDetailList;
        }
        //推送中台任务:保存任务+发送mq
        syncWdtVirtualWarehousePushOrderService.saveTaskList(pushDetailList,
                allocationEntity.getCode(), SyncOperateEnum.OPERATE_APPROVE.getCode(), SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode());

        return pushDetailList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<VirtualWarehousePushHandleDetailEntity> cancelAllocationPush(VirtualWarehouseAllocationEntity allocationEntity) {
        //查询是否存在分货单拆单主表
        VirtualWarehousePushHandleEntity pushHandleEntity = getByAllocation(allocationEntity);
        List<VirtualWarehousePushHandleDetailEntity> pushDetailList = vmAllocationHandleDetailService.cancelAllocationDetailPush(allocationEntity, pushHandleEntity);
        if (CollUtil.isEmpty(pushDetailList)) {
            return pushDetailList;
        }
        //推送中台任务:保存任务+发送mq
         syncWdtVirtualWarehousePushOrderService.saveTaskList(pushDetailList,
                allocationEntity.getCode(), SyncOperateEnum.OPERATE_APPROVE.getCode(), SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode());
        return pushDetailList;
    }

    @Override
    public void deleteVirtualWarehousePushHandle(String allocationId) {
        //根据分货主表id查询关联表
        List<VirtualWarehousePushHandleRelationEntity> relationList = virtualWarehousePushHandleRelationService.list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                .eq(VirtualWarehousePushHandleRelationEntity::getSourceId, allocationId));
        if (CollUtil.isNotEmpty(relationList)) {
            return;
        }
        VirtualWarehousePushHandleEntity pushHandleEntity = getBySourceId(allocationId);
        if (ObjUtil.isEmpty(pushHandleEntity)) {
            return;
        }
        this.forceDeleteById(pushHandleEntity.getId());
    }


    /**
     * 查询分货单拆单主表
     * @author will
     * @date 2026/1/27 15:19
     * @param allocationEntity
     * @return VirtualWarehousePushHandleEntity
     */
    public VirtualWarehousePushHandleEntity getByAllocation(VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualWarehousePushHandleEntity oldPushHandleEntity = getBySourceId(allocationEntity.getId());
        if (ObjUtil.isEmpty(oldPushHandleEntity)) {
            return oldPushHandleEntity;
        }
        VirtualWarehousePushHandleEntity pushHandleEntity = new VirtualWarehousePushHandleEntity(allocationEntity.getId(),
                allocationEntity.getCode(),SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),allocationEntity.getStatus(),allocationEntity.getDirection());
        super.save(pushHandleEntity);
        return  pushHandleEntity;
    }

    /**
     * 根据来源id查询
     * @author will
     * @date 2026/1/27 15:17
     * @param sourceId
     * @return VirtualWarehousePushHandleEntity
     */
    private VirtualWarehousePushHandleEntity getBySourceId(String sourceId) {
        return lambdaQuery().eq(VirtualWarehousePushHandleEntity::getSourceId,sourceId).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehousePushHandleEntity virtualWarehousePushHandleEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
