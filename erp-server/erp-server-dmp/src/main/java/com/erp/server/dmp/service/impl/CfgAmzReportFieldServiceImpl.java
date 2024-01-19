package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.CfgAmzReportFieldEntity;
import com.erp.server.dmp.mapper.CfgAmzReportFieldMapper;
import com.erp.server.dmp.service.CfgAmzReportFieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
 * @since 2024-01-18
 */
@Slf4j
@Service
public class CfgAmzReportFieldServiceImpl extends SuperServiceImpl<CfgAmzReportFieldMapper, CfgAmzReportFieldEntity> implements CfgAmzReportFieldService {

    @Override
    public Map<String, String> mayByReportType(String recordType) {
        List<CfgAmzReportFieldEntity> list = lambdaQuery()
                .eq(CfgAmzReportFieldEntity::getReportType, recordType)
                .eq(CfgAmzReportFieldEntity::getStatus, true)
                .list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.toMap(CfgAmzReportFieldEntity::getColumnName, CfgAmzReportFieldEntity::getFieldName));
    }
}
