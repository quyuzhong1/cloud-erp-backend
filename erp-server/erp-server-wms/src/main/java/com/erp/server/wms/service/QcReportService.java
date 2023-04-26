package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.entity.QcReportEntity;

import javax.servlet.http.HttpServletResponse;
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

    
    /**
     * 根据质检类型 获取待 报告明细
     * @author yl
     * @date 2023-04-18 16:01
     * @param qcType
     * @return java.util.List<com.erp.model.wms.dto.QcReportDTO.ListDTO>
     */
    List<QcReportDTO.ListDTO> getByQcType(String qcType);

    /**
     * 方法说明
     * @author yl
     * @date 2023-04-18 16:27
     * @param ruleIds
     * @return void
     */
    void removeByRuleIds(List<String> ruleIds);


    /**
     * 导出质检单报告
     * @author yl
     * @date 2023-04-21 18:54
     * @return void
     */
    void exportQcReport(QcReportDetailDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 根据质检类型获取质检报告信息
     * @author yl
     * @date 2023-04-26 9:50
     * @param qcTypeList
     * @return java.util.List<com.erp.model.wms.dto.QcReportDTO.ListDTO>
     */
    List<QcReportDTO.ListDTO> listByQcType(List<String> qcTypeList);
}
