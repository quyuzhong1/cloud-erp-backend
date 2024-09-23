package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;
import com.erp.server.wms.mapper.ReportOrderDemandDetailMapper;
import com.erp.server.wms.service.ReportOrderDemandDetailService;
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
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 订单需求明细报表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderDemandDetailServiceImpl extends SuperServiceImpl<ReportOrderDemandDetailMapper, ReportOrderDemandDetailEntity> implements ReportOrderDemandDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportOrderDemandDetailDTO.AddDTO addDTO) {
        ReportOrderDemandDetailEntity reportOrderDemandDetailEntity = new ReportOrderDemandDetailEntity();
        BeanMapperUtils.copy(addDTO, reportOrderDemandDetailEntity);

        // 数据处理
        handleData(reportOrderDemandDetailEntity);

        log.info("开始新增订单需求明细报单");
        boolean save = super.save(reportOrderDemandDetailEntity);
        if(!save) {
            throw new ServiceException("订单需求明细报单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单需求明细报单" , reportOrderDemandDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderDemandDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderDemandDetailEntity.getId(), reportOrderDemandDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderDemandDetailDTO.UpdateDTO updateDTO) {
        ReportOrderDemandDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单需求明细报单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        ReportOrderDemandDetailEntity reportOrderDemandDetailEntity =  BeanMapperUtils.map(ReportOrderDemandDetailEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderDemandDetailEntity);
        log.info("编辑 开始修改订单需求明细报单数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderDemandDetailEntity);
        if(!save) {
            throw new ServiceException("订单需求明细报单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录订单需求明细报单日志数据，id：【{}】", reportOrderDemandDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderDemandDetailEntity.getId(), "订单需求明细报单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderDemandDetailEntity, null, reportOrderDemandDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderDemandDetailEntity reportOrderDemandDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
