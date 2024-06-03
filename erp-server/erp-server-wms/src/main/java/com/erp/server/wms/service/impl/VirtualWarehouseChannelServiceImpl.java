package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.server.wms.mapper.VirtualWarehouseChannelMapper;
import com.erp.server.wms.service.VirtualWarehouseChannelService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseChannelDTO;
import java.util.*;
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
        if(!save) {
            throw new ServiceException("虚拟仓渠道保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓渠道" , virtualWarehouseChannelEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseChannelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseChannelEntity.getId(), virtualWarehouseChannelEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseChannelDTO.UpdateDTO updateDTO) {
        VirtualWarehouseChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓渠道"));
        VirtualWarehouseChannelEntity virtualWarehouseChannelEntity =  BeanMapperUtils.map(VirtualWarehouseChannelEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseChannelEntity);
        log.info("编辑 开始修改虚拟仓渠道数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseChannelEntity);
        if(!save) {
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
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehouseChannelEntity virtualWarehouseChannelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
