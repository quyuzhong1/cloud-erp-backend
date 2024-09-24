package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 亚马逊报告请求记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface DmpAmzReportInfoService extends SuperService<DmpAmzReportInfoEntity> {

    /**
     * 根据报告ID查询信息
     *
     * @author Jim
     * @date: 2024-01-19
     */

    DmpAmzReportInfoEntity getByReportId(String reportId, String processingStatus);
}
