package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.entity.ReportPeriodMonthEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.ReportPeriodMonthService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 核算期间月份表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class ReportPeriodMonthServiceImpl extends SuperServiceImpl<ReportPeriodMonthMapper, ReportPeriodMonthEntity> implements ReportPeriodMonthService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    @Lazy
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportPeriodMonthDTO.AddDTO addDTO) {
        ReportPeriodMonthEntity reportPeriodMonthEntity = new ReportPeriodMonthEntity();
        BeanMapperUtils.copy(addDTO, reportPeriodMonthEntity);

        // 数据处理
        handleData(reportPeriodMonthEntity);

        log.info("开始新增核算期间月份单");
        boolean save = super.save(reportPeriodMonthEntity);
        if(!save) {
            throw new ServiceException("核算期间月份单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "核算期间月份单" , reportPeriodMonthEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, reportPeriodMonthEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(reportPeriodMonthEntity.getId(), reportPeriodMonthEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportPeriodMonthDTO.UpdateDTO updateDTO) {
        ReportPeriodMonthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "核算期间月份单"));
        ReportPeriodMonthEntity reportPeriodMonthEntity =  BeanMapperUtils.map(ReportPeriodMonthEntity.class, updateDTO);

        // 数据处理
        handleData(reportPeriodMonthEntity);
        log.info("编辑 开始修改核算期间月份单数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportPeriodMonthEntity);
        if(!save) {
            throw new ServiceException("核算期间月份单保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录核算期间月份单日志数据，id：【{}】", reportPeriodMonthEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportPeriodMonthEntity.getId(), "核算期间月份单");
        
        operateLogService.addModuleOperateLogByObj(old, reportPeriodMonthEntity, null, reportPeriodMonthEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public String createOrUpdatePeriod(SysAccountingCompanyEntity company, FirstMileCostAllocationEntity entity) {
        ReportPeriodMonthEntity reportPeriodMonthEntity = null;
        if (Objects.nonNull(entity) && CharSequenceUtil.isNotBlank(entity.getReportPeriodId())){
            reportPeriodMonthEntity = this.getById(entity.getReportPeriodId());
        }
        if (Objects.isNull(reportPeriodMonthEntity)){
            LocalDate reportPeriodMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);//上月第一天
            if (Objects.nonNull(entity) && Objects.nonNull(entity.getReportPeriodMonth())){
                reportPeriodMonth = entity.getReportPeriodMonth().withDayOfMonth(1);
            }
            entity.setReportPeriodMonth(reportPeriodMonth);
            //检查是否存在组织对应的核算记录
            List<ReportPeriodMonthEntity> list = this.lambdaQuery().eq(ReportPeriodMonthEntity::getOrgId, company.getId())
                    .eq(ReportPeriodMonthEntity::getMonth, reportPeriodMonth).list();
            if (CollectionUtils.isEmpty(list)){
                reportPeriodMonthEntity = new ReportPeriodMonthEntity();
                reportPeriodMonthEntity.setMonth(reportPeriodMonth);
                reportPeriodMonthEntity.setOrgId(company.getId());
                reportPeriodMonthEntity.setOrgName(company.getCompanyName());
                this.save(reportPeriodMonthEntity);
                return reportPeriodMonthEntity.getId();
            }else {
                return list.get(0).getId();
            }
        }else {
            entity.setReportPeriodMonth(reportPeriodMonthEntity.getMonth());
            return reportPeriodMonthEntity.getId();
        }
    }

    @Override
    public List<ReportPeriodMonthDTO.SelectDTO> queryList(ReportPeriodMonthDTO.QueryDTO dto) {
        List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(firstMileWeightAllocationEntities)){
            throw new ServiceException("重量分摊记录为空");
        }
        List<String> deliveryIds = firstMileWeightAllocationEntities.stream().map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryIds)){
            throw new ServiceException("发货单ids为空");
        }
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntities)){
            throw new ServiceException("发货单记录为空");
        }
        //目的仓ids
        List<String> toWarehouseIds = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getDestWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(toWarehouseIds);
        List<String> orgIds = updateDTOS.stream().map(WarehouseDTO.UpdateDTO::getOrgId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIds)){
            throw new ServiceException("核算组织不能为空");
        }
        if (orgIds.size() > 1){
            throw new ServiceException("不同的核算组织不能同时下推费用分摊");
        }
        List<ReportPeriodMonthDTO.SelectDTO> selectDTOS = baseMapper.queryList(orgIds);
        //已生成的费用分摊记录
        List<FirstMileCostAllocationEntity> firstMileCostAllocationEntityList = firstMileCostAllocationService.listBySourceIds(deliveryIds, null, null, null);
        if (!CollectionUtils.isEmpty(firstMileCostAllocationEntityList)){
            FirstMileCostAllocationEntity entity = firstMileCostAllocationEntityList.stream().filter(e -> Objects.nonNull(e) && ConfirmStatusEnum.CONFIRM.getCode().equals(e.getStatus())).max(Comparator.comparing(FirstMileCostAllocationEntity::getReportPeriodMonth)).orElse(null);
            if (Objects.nonNull(entity)){
                selectDTOS = selectDTOS.stream().filter(e -> e.getReportPeriodMonth().isAfter(entity.getReportPeriodMonth())).collect(Collectors.toList());
            }
        }
        return selectDTOS;
    }

    @Override
    public List<ReportPeriodMonthDTO.ListDTO> listLocalDate() {
        return baseMapper.listLocalDate();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportPeriodMonthEntity reportPeriodMonthEntity) {
    
    }
}
