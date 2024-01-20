package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.enums.ReportScheduleSubscribedTypeEnum;
import com.erp.server.dmp.mapper.CfgAmzReportTypeMapper;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告类型配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
@Slf4j
@Service
public class CfgAmzReportTypeServiceImpl extends SuperServiceImpl<CfgAmzReportTypeMapper, CfgAmzReportTypeEntity> implements CfgAmzReportTypeService {

    @Override
    public List<CfgAmzReportTypeEntity> findActive() {
        return this.lambdaQuery()
                .eq(CfgAmzReportTypeEntity::getDisabled, false)
                .eq(CfgAmzReportTypeEntity::getSubscribedType, ReportScheduleSubscribedTypeEnum.MANUAL.getCode())
                .list();
    }

    @Override
    public Map<String, List<CfgAmzReportTypeEntity>> mapByReportGroup() {
        List<CfgAmzReportTypeEntity> list = this.findActive();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.groupingBy(CfgAmzReportTypeEntity::getReportGroup));
    }

    @Override
    public CfgAmzReportTypeEntity getByRecordType(String reportType) {
        return lambdaQuery()
                .eq(CfgAmzReportTypeEntity::getReportType, reportType)
                .eq(CfgAmzReportTypeEntity::getDisabled, false)
                .last("LIMIT 1")
                .one();
    }
}
