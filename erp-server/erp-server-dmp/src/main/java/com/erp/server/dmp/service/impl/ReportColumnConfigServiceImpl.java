package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.ReportColumnConfigEntity;
import com.erp.server.dmp.mapper.ReportColumnConfigMapper;
import com.erp.server.dmp.service.ReportColumnConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告字段配置 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-12-21
 */
@Slf4j
@Service
public class ReportColumnConfigServiceImpl extends SuperServiceImpl<ReportColumnConfigMapper, ReportColumnConfigEntity> implements ReportColumnConfigService {


    @Override
    public Map<String, String> mayByReportType(String recordType) {
        List<ReportColumnConfigEntity> list = lambdaQuery()
                .eq(ReportColumnConfigEntity::getReportType, recordType)
                .eq(ReportColumnConfigEntity::getStatus, true)
                .list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(ReportColumnConfigEntity::getColumnName, ReportColumnConfigEntity::getFieldName));
    }
}
