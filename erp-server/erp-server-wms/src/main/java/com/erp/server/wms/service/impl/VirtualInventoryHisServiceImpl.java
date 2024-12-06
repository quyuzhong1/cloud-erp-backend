package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.erp.server.wms.mapper.VirtualInventoryHisMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualInventoryHisService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 虚拟仓库存历史信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryHisServiceImpl extends SuperServiceImpl<VirtualInventoryHisMapper, VirtualInventoryHisEntity> implements VirtualInventoryHisService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualInventoryHisDTO.AddDTO addDTO) {
        VirtualInventoryHisEntity virtualInventoryHisEntity = new VirtualInventoryHisEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryHisEntity);

        // 数据处理
        handleData(virtualInventoryHisEntity);

        log.info("开始新增虚拟仓库存历史信息");
        boolean save = super.save(virtualInventoryHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库存历史信息" , virtualInventoryHisEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualInventoryHisEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualInventoryHisEntity.getId(), virtualInventoryHisEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualInventoryHisDTO.UpdateDTO updateDTO) {
        VirtualInventoryHisEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库存历史信息"));
        VirtualInventoryHisEntity virtualInventoryHisEntity =  BeanMapperUtils.map(VirtualInventoryHisEntity.class, updateDTO);

        // 数据处理
        handleData(virtualInventoryHisEntity);
        log.info("编辑 开始修改虚拟仓库存历史信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualInventoryHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库存历史信息日志数据，id：【{}】", virtualInventoryHisEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualInventoryHisEntity.getId(), "虚拟仓库存历史信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualInventoryHisEntity, null, virtualInventoryHisEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<VirtualInventoryHisEntity> listByParam(VirtualInventoryHisDTO.ParamDTO paramDTO) {
        return baseMapper.listByParam(paramDTO);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryHisEntity virtualInventoryHisEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
