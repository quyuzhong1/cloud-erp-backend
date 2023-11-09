package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.ReportScheduleEntity;
import com.erp.server.dmp.mapper.ReportScheduleMapper;
import com.erp.server.dmp.service.ReportScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ReportScheduleServiceImpl extends SuperServiceImpl<ReportScheduleMapper, ReportScheduleEntity> implements ReportScheduleService {

    @Override
    public boolean existByReportScheduleId(String reportScheduleId) {
        Integer count = lambdaQuery()
                .eq(ReportScheduleEntity::getReportScheduleId, reportScheduleId)
                .count();
        return count > 0;
    }

    @Override
    public ReportScheduleEntity getByReportScheduleId(String reportScheduleId) {
        return lambdaQuery()
                .eq(ReportScheduleEntity::getReportScheduleId, reportScheduleId)
                .last("LIMIT 1")
                .one();
    }
}
