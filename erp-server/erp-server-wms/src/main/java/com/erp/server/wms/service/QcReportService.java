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

    /**
     * 根据质检规则id获取质检报告信息
     * @author yl
     * @date 2023-04-13 14:26
     * @param qcRuleId
     * @return java.util.List<com.erp.model.wms.dto.QcReportDTO.UpdateDTO>
     */
    List<QcReportDTO.UpdateDTO> getByQcRuleId(String qcRuleId);

    /**
     * 修改质检报告
     * @author yl
     * @date 2023-04-13 14:53
     * @param qcRuleId
     * @param qcReportLList
     * @return void
     */
    void updateQcReport(String qcRuleId, List<QcReportDTO.UpdateDTO> qcReportLList);
}
