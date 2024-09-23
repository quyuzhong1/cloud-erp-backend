package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ReportOrderSalesRefEntity;
import com.erp.server.wms.mapper.ReportOrderSalesRefMapper;
import com.erp.server.wms.service.ReportOrderSalesRefService;
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
import com.erp.model.wms.dto.ReportOrderSalesRefDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 订单销量关联表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderSalesRefServiceImpl extends SuperServiceImpl<ReportOrderSalesRefMapper, ReportOrderSalesRefEntity> implements ReportOrderSalesRefService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportOrderSalesRefDTO.AddDTO addDTO) {
        ReportOrderSalesRefEntity reportOrderSalesRefEntity = new ReportOrderSalesRefEntity();
        BeanMapperUtils.copy(addDTO, reportOrderSalesRefEntity);

        // 数据处理
        handleData(reportOrderSalesRefEntity);

        log.info("开始新增订单销量关联单");
        boolean save = super.save(reportOrderSalesRefEntity);
        if(!save) {
            throw new ServiceException("订单销量关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单销量关联单" , reportOrderSalesRefEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderSalesRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderSalesRefEntity.getId(), reportOrderSalesRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderSalesRefDTO.UpdateDTO updateDTO) {
        ReportOrderSalesRefEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单销量关联单"));
        ReportOrderSalesRefEntity reportOrderSalesRefEntity =  BeanMapperUtils.map(ReportOrderSalesRefEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderSalesRefEntity);
        log.info("编辑 开始修改订单销量关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderSalesRefEntity);
        if(!save) {
            throw new ServiceException("订单销量关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录订单销量关联单日志数据，id：【{}】", reportOrderSalesRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderSalesRefEntity.getId(), "订单销量关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderSalesRefEntity, null, reportOrderSalesRefEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderSalesRefEntity reportOrderSalesRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
