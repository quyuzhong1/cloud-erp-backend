package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.entity.ReportOrderDataEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.wms.mapper.ReportOrderDataMapper;
import com.erp.server.wms.service.CfgSettingVirtualService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.ReportOrderDataService;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 订单报表信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-24
 */
@Slf4j
@Service
public class ReportOrderDataServiceImpl extends SuperServiceImpl<ReportOrderDataMapper, ReportOrderDataEntity> implements ReportOrderDataService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private CfgSettingVirtualService cfgSettingVirtualService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportOrderDataDTO.AddDTO addDTO) {
        ReportOrderDataEntity reportOrderDataEntity = new ReportOrderDataEntity();
        BeanMapperUtils.copy(addDTO, reportOrderDataEntity);

        // 数据处理
        handleData(reportOrderDataEntity);

        log.info("开始新增订单报表信息");
        boolean save = super.save(reportOrderDataEntity);
        if(!save) {
            throw new ServiceException("订单报表信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单报表信息" , reportOrderDataEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderDataEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderDataEntity.getId(), reportOrderDataEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderDataDTO.UpdateDTO updateDTO) {
        ReportOrderDataEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "订单报表信息"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        ReportOrderDataEntity reportOrderDataEntity =  BeanMapperUtils.map(ReportOrderDataEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderDataEntity);
        log.info("编辑 开始修改订单报表信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderDataEntity);
        if(!save) {
            throw new ServiceException("订单报表信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录订单报表信息日志数据，id：【{}】", reportOrderDataEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderDataEntity.getId(), "订单报表信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderDataEntity, null, reportOrderDataEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void generateReportOrderData() {
        //b2b销售订单
        List<ReportOrderDataDTO.ViewDTO> soDetailList = soInfoFeign.listAllVirtualSoDetail();

        //b2c销售订单
        List<ReportOrderDataDTO.ViewDTO> soB2cDetailList = soB2cFeign.listAllVirtualSoB2cDetail();

        //要货申请
        List<ReportOrderDataDTO.ViewDTO> requisitionApplicationDetailList = requisitionApplicationDetailService.listAllVirtualRequisitionApplicationDetail();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderDataEntity reportOrderDataEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
