package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationDetailMapper;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 虚拟仓分货单明细 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationDetailServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationDetailMapper, VirtualWarehouseAllocationDetailEntity> implements VirtualWarehouseAllocationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDetailDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = new VirtualWarehouseAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationDetailEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);

        log.info("开始新增虚拟仓分货单明细");
        boolean save = super.save(virtualWarehouseAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓分货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓分货单明细" , virtualWarehouseAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationDetailEntity.getId(), virtualWarehouseAllocationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓分货单明细"));
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity =  BeanMapperUtils.map(VirtualWarehouseAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);
        log.info("编辑 开始修改虚拟仓分货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓分货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓分货单明细日志数据，id：【{}】", virtualWarehouseAllocationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationDetailEntity.getId(), "虚拟仓分货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationDetailEntity, null, virtualWarehouseAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
