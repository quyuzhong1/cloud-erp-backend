package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.entity.InvoiceTaxEntity;
import com.erp.server.oms.mapper.InvoiceTaxMapper;
import com.erp.server.oms.service.InvoiceTaxService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 发票税务信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@Service
public class InvoiceTaxServiceImpl extends SuperServiceImpl<InvoiceTaxMapper, InvoiceTaxEntity> implements InvoiceTaxService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceTaxDTO.AddDTO addDTO) {
        InvoiceTaxEntity invoiceTaxEntity = new InvoiceTaxEntity();
        BeanMapperUtils.copy(addDTO, invoiceTaxEntity);

        // 数据处理
        handleData(invoiceTaxEntity);

        log.info("开始新增发票税务信息");
        boolean save = super.save(invoiceTaxEntity);
        if(!save) {
            throw new ServiceException("发票税务信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票税务信息" , invoiceTaxEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, invoiceTaxEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(invoiceTaxEntity.getId(), invoiceTaxEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceTaxDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceTaxEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发票税务信息"));
        InvoiceTaxEntity invoiceTaxEntity =  BeanMapperUtils.map(InvoiceTaxEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceTaxEntity);
        log.info("编辑 开始修改发票税务信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(invoiceTaxEntity);
        if(!save) {
            throw new ServiceException("发票税务信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发票税务信息日志数据，id：【{}】", invoiceTaxEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceTaxEntity.getId(), "发票税务信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceTaxEntity, null, invoiceTaxEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public InvoiceTaxDTO.ViewDTO view(String id) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceTaxEntity invoiceTaxEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
