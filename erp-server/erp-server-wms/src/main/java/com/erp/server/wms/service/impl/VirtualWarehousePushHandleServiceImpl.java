package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleEntity;
import com.erp.server.wms.mapper.VirtualWarehousePushHandleMapper;
import com.erp.server.wms.service.VirtualWarehousePushHandleDetailService;
import com.erp.server.wms.service.VirtualWarehousePushHandleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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

    @GlobalTransactional(rollbackFor = Exception.class)
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
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单主单");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单主单"));
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleData(VirtualWarehouseAllocationEntity allocationEntity) {
        //保存合单主表
        VirtualWarehousePushHandleEntity pushHandleEntity = new VirtualWarehousePushHandleEntity(allocationEntity.getId(),
                allocationEntity.getCode(),SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),allocationEntity.getStatus(),allocationEntity.getDirection());
        this.save(pushHandleEntity);
        //保存拆单明细表
        vmAllocationHandleDetailService.handleDetail(allocationEntity,pushHandleEntity);
    }

    @Override
    public void forceDeleteById(String id) {
        baseMapper.forceDeleteById(id);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehousePushHandleEntity virtualWarehousePushHandleEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
