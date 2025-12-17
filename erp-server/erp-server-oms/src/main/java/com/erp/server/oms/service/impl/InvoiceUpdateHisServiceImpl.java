package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.InvoiceUpdateHisDTO;
import com.erp.model.oms.entity.InvoiceUpdateHisEntity;
import com.erp.server.oms.mapper.InvoiceUpdateHisMapper;
import com.erp.server.oms.service.InvoiceUpdateHisService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 发票更新历史 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@Service
public class InvoiceUpdateHisServiceImpl extends SuperServiceImpl<InvoiceUpdateHisMapper, InvoiceUpdateHisEntity> implements InvoiceUpdateHisService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceUpdateHisDTO.AddDTO addDTO) {
        InvoiceUpdateHisEntity invoiceUpdateHisEntity = new InvoiceUpdateHisEntity();
        BeanMapperUtils.copy(addDTO, invoiceUpdateHisEntity);

        // 数据处理
        handleData(invoiceUpdateHisEntity);

        log.info("开始新增发票更新历史");
        boolean save = super.save(invoiceUpdateHisEntity);
        if(!save) {
            throw new ServiceException("发票更新历史保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票更新历史" , invoiceUpdateHisEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, invoiceUpdateHisEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(invoiceUpdateHisEntity.getId(), invoiceUpdateHisEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceUpdateHisDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceUpdateHisEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发票更新历史"));
        InvoiceUpdateHisEntity invoiceUpdateHisEntity =  BeanMapperUtils.map(InvoiceUpdateHisEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceUpdateHisEntity);
        log.info("编辑 开始修改发票更新历史数据，id：【{}】", old.getId());
        boolean save = super.updateById(invoiceUpdateHisEntity);
        if(!save) {
            throw new ServiceException("发票更新历史保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发票更新历史日志数据，id：【{}】", invoiceUpdateHisEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceUpdateHisEntity.getId(), "发票更新历史");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceUpdateHisEntity, null, invoiceUpdateHisEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<InvoiceUpdateHisDTO.ListDTO> list(InvoiceUpdateHisDTO.IdDTO dto) {
        return baseMapper.list(dto);
    }

    @Override
    public Integer countByInvoiceInfoId(String invoiceInfoId) {
        return baseMapper.countByInvoiceInfoId(invoiceInfoId);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceUpdateHisEntity invoiceUpdateHisEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
