package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleRelationEntity;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationHandleRelationMapper;
import com.erp.server.wms.service.VirtualWarehouseAllocationHandleRelationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleRelationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
public class VirtualWarehouseAllocationHandleRelationServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationHandleRelationMapper, VirtualWarehouseAllocationHandleRelationEntity> implements VirtualWarehouseAllocationHandleRelationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationHandleRelationDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationHandleRelationEntity virtualWarehouseAllocationHandleRelationEntity = new VirtualWarehouseAllocationHandleRelationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationHandleRelationEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationHandleRelationEntity);

        log.info("开始新增分货单拆单关联关系单");
        boolean save = super.save(virtualWarehouseAllocationHandleRelationEntity);
        if(!save) {
            throw new ServiceException("分货单拆单关联关系单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单拆单关联关系单" , virtualWarehouseAllocationHandleRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationHandleRelationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationHandleRelationEntity.getId(), virtualWarehouseAllocationHandleRelationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationHandleRelationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationHandleRelationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单关联关系单"));
        VirtualWarehouseAllocationHandleRelationEntity virtualWarehouseAllocationHandleRelationEntity =  BeanMapperUtils.map(VirtualWarehouseAllocationHandleRelationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationHandleRelationEntity);
        log.info("编辑 开始修改分货单拆单关联关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseAllocationHandleRelationEntity);
        if(!save) {
            throw new ServiceException("分货单拆单关联关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录分货单拆单关联关系单日志数据，id：【{}】", virtualWarehouseAllocationHandleRelationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationHandleRelationEntity.getId(), "分货单拆单关联关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationHandleRelationEntity, null, virtualWarehouseAllocationHandleRelationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehouseAllocationHandleRelationEntity virtualWarehouseAllocationHandleRelationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
