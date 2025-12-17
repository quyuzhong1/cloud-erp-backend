package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.SupplierPurchaseQuantityEntity;
import com.erp.server.scm.mapper.SupplierPurchaseQuantityMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierPurchaseQuantityService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.SupplierPurchaseQuantityDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 供应商采购数量 服务实现类
 * </p>
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@Service
public class SupplierPurchaseQuantityServiceImpl extends SuperServiceImpl<SupplierPurchaseQuantityMapper, SupplierPurchaseQuantityEntity> implements SupplierPurchaseQuantityService {
    @Autowired
    private ModuleOperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierPurchaseQuantityDTO.AddDTO addDTO) {
        SupplierPurchaseQuantityEntity supplierPurchaseQuantityEntity = new SupplierPurchaseQuantityEntity();
        BeanMapperUtils.copy(addDTO, supplierPurchaseQuantityEntity);

        // 数据处理
        handleData(supplierPurchaseQuantityEntity);

        log.info("开始新增供应商采购数量");
        boolean save = super.save(supplierPurchaseQuantityEntity);
        if(!save) {
            throw new ServiceException("供应商采购数量保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "供应商采购数量" , supplierPurchaseQuantityEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, supplierPurchaseQuantityEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(supplierPurchaseQuantityEntity.getId(), supplierPurchaseQuantityEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierPurchaseQuantityDTO.UpdateDTO addOrUpdateDTO) {
        SupplierPurchaseQuantityEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "供应商采购数量"));
        SupplierPurchaseQuantityEntity supplierPurchaseQuantityEntity =  BeanMapperUtils.map(SupplierPurchaseQuantityEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(supplierPurchaseQuantityEntity);
        log.info("编辑 开始修改供应商采购数量数据，id：【{}】", old.getId());
        boolean save = super.updateById(supplierPurchaseQuantityEntity);
        if(!save) {
            throw new ServiceException("供应商采购数量保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录供应商采购数量日志数据，id：【{}】", supplierPurchaseQuantityEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierPurchaseQuantityEntity.getId(), "供应商采购数量");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, supplierPurchaseQuantityEntity, null, supplierPurchaseQuantityEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SupplierPurchaseQuantityEntity supplierPurchaseQuantityEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
