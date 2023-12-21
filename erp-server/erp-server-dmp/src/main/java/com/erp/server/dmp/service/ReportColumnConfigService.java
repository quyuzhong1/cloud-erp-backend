package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.ReportColumnConfigEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ReportColumnConfigDTO;

import java.util.Map;

/**
 * <p>
 * 亚马逊报告字段配置 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-12-21
 */
public interface ReportColumnConfigService extends SuperService<ReportColumnConfigEntity> {

    /**
    * 修改
    * @author Jim
    * @date: 2023-12-21
    * @param recordType 报告类型
    * @return map<报告列表名, mongo保存字段名>
    */
    Map<String, String> mayByReportType(String recordType);
}
