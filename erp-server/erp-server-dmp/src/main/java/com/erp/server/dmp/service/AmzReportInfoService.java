package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.AmzReportInfoEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 亚马逊报告请求记录 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-01-18
 */
public interface AmzReportInfoService extends SuperService<AmzReportInfoEntity> {

    /**
     * 根据报告ID查询信息
     *
     * @author Jim
     * @date: 2024-01-19
     */

    AmzReportInfoEntity getByReportId(String reportId, String processingStatus);
}
