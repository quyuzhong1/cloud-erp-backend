package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelPartitionRefEntity;
import com.erp.server.wms.mapper.VirtualWarehouseChannelPartitionRefMapper;
import com.erp.server.wms.service.VirtualWarehouseChannelPartitionRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseChannelPartitionRefDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 虚拟仓渠道分区关联表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-01-03
 */
@Slf4j
@Service
public class VirtualWarehouseChannelPartitionRefServiceImpl extends SuperServiceImpl<VirtualWarehouseChannelPartitionRefMapper, VirtualWarehouseChannelPartitionRefEntity> implements VirtualWarehouseChannelPartitionRefService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseChannelPartitionRefDTO.AddDTO addDTO) {
        VirtualWarehouseChannelPartitionRefEntity virtualWarehouseChannelPartitionRefEntity = new VirtualWarehouseChannelPartitionRefEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseChannelPartitionRefEntity);

        // 数据处理
        handleData(virtualWarehouseChannelPartitionRefEntity);

        log.info("开始新增虚拟仓渠道分区关联单");
        boolean save = super.save(virtualWarehouseChannelPartitionRefEntity);
        if(!save) {
            throw new ServiceException("虚拟仓渠道分区关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓渠道分区关联单" , virtualWarehouseChannelPartitionRefEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseChannelPartitionRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseChannelPartitionRefEntity.getId(), virtualWarehouseChannelPartitionRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseChannelPartitionRefDTO.UpdateDTO updateDTO) {
        VirtualWarehouseChannelPartitionRefEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓渠道分区关联单"));
        VirtualWarehouseChannelPartitionRefEntity virtualWarehouseChannelPartitionRefEntity =  BeanMapperUtils.map(VirtualWarehouseChannelPartitionRefEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseChannelPartitionRefEntity);
        log.info("编辑 开始修改虚拟仓渠道分区关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseChannelPartitionRefEntity);
        if(!save) {
            throw new ServiceException("虚拟仓渠道分区关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓渠道分区关联单日志数据，id：【{}】", virtualWarehouseChannelPartitionRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseChannelPartitionRefEntity.getId(), "虚拟仓渠道分区关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseChannelPartitionRefEntity, null, virtualWarehouseChannelPartitionRefEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void deleteByMainIds(List<String> oldChannelIds) {
        if (CollUtil.isEmpty(oldChannelIds)){
            return;
        }
        this.lambdaUpdate().in(VirtualWarehouseChannelPartitionRefEntity::getMainId,oldChannelIds).set(VirtualWarehouseChannelPartitionRefEntity::getIsDeleted,true).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByMainId(String mainId, List<String> partitionIds) {
        if (CollUtil.isEmpty(partitionIds) || CharSequenceUtil.isBlank(mainId)){
            return;
        }
        List<VirtualWarehouseChannelPartitionRefEntity> entityList = new ArrayList<>(partitionIds.size());
        for (int i = 0; i < partitionIds.size(); i++) {
            VirtualWarehouseChannelPartitionRefEntity entity = new VirtualWarehouseChannelPartitionRefEntity()
                    .setPartitionId(partitionIds.get(i))
                    .setMainId(mainId).setIndex(i);
            entityList.add(entity);
        }
        if (CollUtil.isNotEmpty(entityList)){
            this.saveBatch(entityList);
        }
    }

    @Override
    public List<VirtualWarehouseChannelPartitionRefEntity> listByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(VirtualWarehouseChannelPartitionRefEntity::getMainId, mainIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehouseChannelPartitionRefEntity virtualWarehouseChannelPartitionRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
