package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.server.wms.mapper.WarehouseLocationMoveDetailMapper;
import com.erp.server.wms.service.WarehouseLocationMoveDetailService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 仓位移动明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Slf4j
@Service
public class WarehouseLocationMoveDetailServiceImpl extends SuperServiceImpl<WarehouseLocationMoveDetailMapper, WarehouseLocationMoveDetailEntity> implements WarehouseLocationMoveDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(WarehouseLocationMoveDetailDTO.AddDTO addDTO) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = new WarehouseLocationMoveDetailEntity();
        BeanMapperUtils.copy(addDTO, warehouseLocationMoveDetailEntity);

        // 数据处理
        handleData(warehouseLocationMoveDetailEntity);

        log.info("开始新增仓位移动明细单");
        boolean save = super.save(warehouseLocationMoveDetailEntity);
        if(!save) {
            throw new ServiceException("仓位移动明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "仓位移动明细单" , warehouseLocationMoveDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, warehouseLocationMoveDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return warehouseLocationMoveDetailEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseLocationMoveDetailDTO.UpdateDTO updateDTO) {
        WarehouseLocationMoveDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "仓位移动明细单"));
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity =  BeanMapperUtils.map(WarehouseLocationMoveDetailEntity.class, updateDTO);

        // 数据处理
        handleData(warehouseLocationMoveDetailEntity);
        log.info("编辑 开始修改仓位移动明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(warehouseLocationMoveDetailEntity);
        if(!save) {
            throw new ServiceException("仓位移动明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录仓位移动明细单日志数据，id：【{}】", warehouseLocationMoveDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), warehouseLocationMoveDetailEntity.getId(), "仓位移动明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, warehouseLocationMoveDetailEntity, null, warehouseLocationMoveDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
