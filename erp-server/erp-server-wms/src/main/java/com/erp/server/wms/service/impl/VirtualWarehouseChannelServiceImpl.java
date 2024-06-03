package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.server.wms.mapper.VirtualWarehouseChannelMapper;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 虚拟仓渠道 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseChannelServiceImpl extends SuperServiceImpl<VirtualWarehouseChannelMapper, VirtualWarehouseChannelEntity> implements VirtualWarehouseChannelService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseChannelDTO.AddDTO addDTO) {
        VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = new VirtualWarehouseChannelEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseChannelEntity);

        // 数据处理
        handleData(virtualWarehouseChannelEntity);

        log.info("开始新增虚拟仓渠道");
        boolean save = super.save(virtualWarehouseChannelEntity);
        if (!save) {
            throw new ServiceException("虚拟仓渠道保存失败");
        }
        return new BaseResultDTO.AddDTO(virtualWarehouseChannelEntity.getId(), virtualWarehouseChannelEntity.getId());
    }

    /**
     * 批量新增
     *
     * @param batchAddDTO
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseChannelDTO.BatchAddDTO batchAddDTO) {
        List<VirtualWarehouseChannelDTO.ChannelAddDTO> dtoList = batchAddDTO.getChannelList();
        //查找原有绑定关系
        List<VirtualWarehouseChannelEntity> existChannelList = baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>()
                .eq(VirtualWarehouseChannelEntity::getVirtualWarehouseId, batchAddDTO.getVirtualWarehouseId()));
        //删除原始绑定
        if (CollectionUtils.isNotEmpty(existChannelList)) {
            baseMapper.deleteBatchIds(existChannelList.stream().map(VirtualWarehouseChannelEntity::getId).collect(Collectors.toList()));
            //新增绑定
            dtoList.forEach(dto->{
                VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = new VirtualWarehouseChannelEntity();
                BeanUtils.copyProperties(dto,virtualWarehouseChannelEntity);
                virtualWarehouseChannelEntity.setVirtualWarehouseId(batchAddDTO.getVirtualWarehouseId());
                super.save(virtualWarehouseChannelEntity);
            });
        }
        return new BaseResultDTO.AddDTO();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseChannelDTO.UpdateDTO updateDTO) {
        VirtualWarehouseChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓渠道"));
        VirtualWarehouseChannelEntity virtualWarehouseChannelEntity = BeanMapperUtils.map(VirtualWarehouseChannelEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseChannelEntity);
        log.info("编辑 开始修改虚拟仓渠道数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseChannelEntity);
        if (!save) {
            throw new ServiceException("虚拟仓渠道保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓渠道日志数据，id：【{}】", virtualWarehouseChannelEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseChannelEntity.getId(), "虚拟仓渠道");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseChannelEntity, null, virtualWarehouseChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }
    /**
     * 通过虚拟仓id获取关联渠道
     *
     * @param virtualWarehouseId
     */
    @Override
    public List<VirtualWarehouseChannelEntity> getByVirtualWarehouseId(String virtualWarehouseId) {
        return baseMapper.selectList(new LambdaQueryWrapper<VirtualWarehouseChannelEntity>().eq(VirtualWarehouseChannelEntity::getVirtualWarehouseId,virtualWarehouseId));
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseChannelEntity virtualWarehouseChannelEntity) {

    }


}
