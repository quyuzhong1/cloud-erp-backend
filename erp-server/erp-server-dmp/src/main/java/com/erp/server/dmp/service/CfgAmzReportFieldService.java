package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgAmzReportFieldEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgAmzReportFieldDTO;

import java.util.Map;

/**
 * <p>
 * 亚马逊报告字段配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface CfgAmzReportFieldService extends SuperService<CfgAmzReportFieldEntity> {

    /**
     * 修改
     * @author Jim
     * @date: 2023-12-21
     * @param recordType 报告类型
     * @return map<报告列表名, mongo保存字段名>
     */
    Map<String, String> mayByReportType(String recordType);
}
