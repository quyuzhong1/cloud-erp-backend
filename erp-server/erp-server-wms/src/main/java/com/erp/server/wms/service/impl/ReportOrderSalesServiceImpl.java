package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import com.erp.server.wms.mapper.ReportOrderSalesMapper;
import com.erp.server.wms.service.ReportOrderSalesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 订单销量表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderSalesServiceImpl extends SuperServiceImpl<ReportOrderSalesMapper, ReportOrderSalesEntity> implements ReportOrderSalesService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportOrderSalesDTO.AddDTO addDTO) {
        ReportOrderSalesEntity reportOrderSalesEntity = new ReportOrderSalesEntity();
        BeanMapperUtils.copy(addDTO, reportOrderSalesEntity);

        // 数据处理
        handleData(reportOrderSalesEntity);

        log.info("开始新增订单销量单");
        boolean save = super.save(reportOrderSalesEntity);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单销量单" , reportOrderSalesEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderSalesEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderSalesEntity.getId(), reportOrderSalesEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderSalesDTO.UpdateDTO updateDTO) {
        ReportOrderSalesEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单销量单"));
        ReportOrderSalesEntity reportOrderSalesEntity =  BeanMapperUtils.map(ReportOrderSalesEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderSalesEntity);
        log.info("编辑 开始修改订单销量单数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderSalesEntity);
        if(!save) {
            throw new ServiceException("订单销量单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录订单销量单日志数据，id：【{}】", reportOrderSalesEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderSalesEntity.getId(), "订单销量单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderSalesEntity, null, reportOrderSalesEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderSalesEntity reportOrderSalesEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
