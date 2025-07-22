package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.SupplierPlantAddrEntity;
import com.erp.server.scm.mapper.SupplierPlantAddrMapper;
import com.erp.server.scm.service.SupplierPlantAddrService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.scm.service.OperateLogService;
import com.erp.server.scm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 供应商工厂地信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-07-21
 */
@Slf4j
@Service
public class SupplierPlantAddrServiceImpl extends SuperServiceImpl<SupplierPlantAddrMapper, SupplierPlantAddrEntity> implements SupplierPlantAddrService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierPlantAddrDTO.AddDTO addDTO) {
        SupplierPlantAddrEntity supplierPlantAddrEntity = new SupplierPlantAddrEntity();
        BeanMapperUtils.copy(addDTO, supplierPlantAddrEntity);

        // 数据处理
        handleData(supplierPlantAddrEntity);

        log.info("开始新增供应商工厂地信息");
        boolean save = super.save(supplierPlantAddrEntity);
        if(!save) {
            throw new ServiceException("供应商工厂地信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "供应商工厂地信息" , supplierPlantAddrEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, supplierPlantAddrEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(supplierPlantAddrEntity.getId(), supplierPlantAddrEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierPlantAddrDTO.UpdateDTO addOrUpdateDTO) {
        SupplierPlantAddrEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "供应商工厂地信息"));
        SupplierPlantAddrEntity supplierPlantAddrEntity =  BeanMapperUtils.map(SupplierPlantAddrEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(supplierPlantAddrEntity);
        log.info("编辑 开始修改供应商工厂地信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(supplierPlantAddrEntity);
        if(!save) {
            throw new ServiceException("供应商工厂地信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录供应商工厂地信息日志数据，id：【{}】", supplierPlantAddrEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierPlantAddrEntity.getId(), "供应商工厂地信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, supplierPlantAddrEntity, null, supplierPlantAddrEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SupplierPlantAddrEntity supplierPlantAddrEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
