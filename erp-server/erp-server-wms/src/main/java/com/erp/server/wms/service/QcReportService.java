package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.entity.QcReportEntity;

import java.util.List;

/**
 * <p>
 * 质检报告 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
public interface QcReportService extends SuperService<QcReportEntity> {

    /**
     * 添加质检报告
     * @author yl
     * @date 2023-04-13 10:31
     * @param id
     * @param reportList
     * @return void
     */
    void addQcReport(String id, List<QcReportDTO.AddDTO> reportList);
}
