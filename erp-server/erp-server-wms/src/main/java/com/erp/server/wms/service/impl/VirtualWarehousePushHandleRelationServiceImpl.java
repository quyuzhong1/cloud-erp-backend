package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.server.wms.mapper.VirtualWarehousePushHandleRelationMapper;
import com.erp.server.wms.service.VirtualWarehousePushHandleRelationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehousePushHandleRelationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 分货单拆单关联关系表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@Service
public class VirtualWarehousePushHandleRelationServiceImpl extends SuperServiceImpl<VirtualWarehousePushHandleRelationMapper, VirtualWarehousePushHandleRelationEntity> implements VirtualWarehousePushHandleRelationService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehousePushHandleRelationDTO.AddDTO addDTO) {
        VirtualWarehousePushHandleRelationEntity virtualWarehousePushHandleRelationEntity = new VirtualWarehousePushHandleRelationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehousePushHandleRelationEntity);

        // 数据处理
        handleData(virtualWarehousePushHandleRelationEntity);

        log.info("开始新增分货单拆单关联关系单");
        boolean save = super.save(virtualWarehousePushHandleRelationEntity);
        if(!save) {
            throw new ServiceException("分货单拆单关联关系单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单拆单关联关系单" , virtualWarehousePushHandleRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehousePushHandleRelationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehousePushHandleRelationEntity.getId(), virtualWarehousePushHandleRelationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehousePushHandleRelationDTO.UpdateDTO updateDTO) {
        VirtualWarehousePushHandleRelationEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单关联关系单");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单关联关系单"));
        VirtualWarehousePushHandleRelationEntity virtualWarehousePushHandleRelationEntity =  BeanMapperUtils.map(VirtualWarehousePushHandleRelationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehousePushHandleRelationEntity);
        log.info("编辑 开始修改分货单拆单关联关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehousePushHandleRelationEntity);
        if(!save) {
            throw new ServiceException("分货单拆单关联关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录分货单拆单关联关系单日志数据，id：【{}】", virtualWarehousePushHandleRelationEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehousePushHandleRelationEntity.getId(), "分货单拆单关联关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehousePushHandleRelationEntity, null, virtualWarehousePushHandleRelationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehousePushHandleRelationEntity virtualWarehousePushHandleRelationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
