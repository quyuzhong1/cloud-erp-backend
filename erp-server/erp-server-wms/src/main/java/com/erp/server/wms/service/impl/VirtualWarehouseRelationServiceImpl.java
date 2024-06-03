package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.server.wms.mapper.VirtualWarehouseRelationMapper;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 虚拟仓实体仓关联关系 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseRelationServiceImpl extends SuperServiceImpl<VirtualWarehouseRelationMapper, VirtualWarehouseRelationEntity> implements VirtualWarehouseRelationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseRelationDTO.AddDTO addDTO) {
        VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = new VirtualWarehouseRelationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseRelationEntity);

        // 数据处理
        handleData(virtualWarehouseRelationEntity);

        log.info("开始新增虚拟仓实体仓关联关系");
        boolean save = super.save(virtualWarehouseRelationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓实体仓关联关系保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓实体仓关联关系", virtualWarehouseRelationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseRelationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseRelationEntity.getId(), virtualWarehouseRelationEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseRelationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseRelationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓实体仓关联关系"));
        VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = BeanMapperUtils.map(VirtualWarehouseRelationEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseRelationEntity);
        log.info("编辑 开始修改虚拟仓实体仓关联关系数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseRelationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓实体仓关联关系保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓实体仓关联关系日志数据，id：【{}】", virtualWarehouseRelationEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseRelationEntity.getId(), "虚拟仓实体仓关联关系");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseRelationEntity, null, virtualWarehouseRelationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseRelationEntity virtualWarehouseRelationEntity) {
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 根据仓库id获取关联关系
     *
     * @param warehouseIdList
     * @return
     */
    @Override
    public List<VirtualWarehouseRelationEntity> getByWarehouseId(List<String> warehouseIdList) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().in(VirtualWarehouseRelationEntity::getWarehouseId, warehouseIdList));
    }

    @Override
    public List<VirtualWarehouseRelationEntity> getByVirtualWarehouseId(String virtualWarehouseId) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().eq(VirtualWarehouseRelationEntity::getVirtualWarehouseId, virtualWarehouseId));
    }

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO) {
        List<String> warehouseIdList = batchAddDTO.getWarehouseIdList();
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            throw new ServiceException(ApiError.ERROR_400);
        }
        List<VirtualWarehouseRelationEntity> existRelationList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseRelationEntity>().in(VirtualWarehouseRelationEntity::getVirtualWarehouseId, batchAddDTO.getVirtualWarehouseId()));
        if (CollectionUtils.isNotEmpty(existRelationList)) {
            //判断原始绑定与变更数据是否相同
            String existWarehouseId = existRelationList.get(0).getWarehouseId();
            if (!Objects.equals(warehouseIdList.get(0), existWarehouseId)) {
                //删除原有绑定关系
                VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = new VirtualWarehouseRelationEntity();
                virtualWarehouseRelationEntity.setWarehouseId(warehouseIdList.get(0));
                virtualWarehouseRelationEntity.setId(existRelationList.get(0).getId());
                baseMapper.updateById(virtualWarehouseRelationEntity);
            }
        }
        return new BaseResultDTO.AddDTO();
    }

}
