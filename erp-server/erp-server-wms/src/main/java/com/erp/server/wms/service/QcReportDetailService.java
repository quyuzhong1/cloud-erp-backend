package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.model.wms.entity.QcReportDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcReportDetailService extends SuperService<QcReportDetailEntity> {

    
    /**
     * 质检报告明细暂存
     * @author yl
     * @date 2023-04-19 11:14
     * @param billId
     * @param reportDetailList
     * @return void
     */
    void add(String billId, List<QcReportDetailDTO.AddDTO> reportDetailList);

    /**
     * 根据质检单id获取 质检报告明细信息
     * @author yl
     * @date 2023-04-19 12:34
     * @param id
     * @return java.util.List<com.erp.model.wms.dto.QcReportDetailDTO.ViewDTO>
     */
    List<QcReportDetailDTO.ViewDTO> getByMainId(String id);
}
