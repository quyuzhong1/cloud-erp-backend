package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcEffectivenessDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/12 11:18
 */
public interface QcEffectivenessService {
    /**
     * @description: 质检总览查询
     * @author Will
     * @date: 2023/4/12 12:04
     * @param dto
     * @return ViewQcOverviewDTO
     */
    QcEffectivenessDTO.ViewQcOverviewDTO viewQcOverview(QcEffectivenessDTO.CommonSearchParamDTO dto);
    /**
     * @description: 质检趋势查询
     * @author Will
     * @date: 2023/4/12 12:07
     * @param dto 
     * @return ViewQcTrendDTO
     */
    QcEffectivenessDTO.ViewQcTrendDTO viewQcTrend(QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto);
    /**
     * @description: 按人员查询
     * @author Will
     * @date: 2023/4/12 12:08
     * @param dto 
     * @return List<ViewQcForPersonnelDTO> 
     */
    PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> viewQcForPersonnel(PagingDTO<QcEffectivenessDTO.CommonSearchParamDTO> dto);
    /**
     * @description: 按单据查询
     * @author Will
     * @date: 2023/4/12 12:08
     * @param dto
     * @return List<ViewQcForDocumentDTO> 
     */
    PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> viewQcForDocument(PagingDTO<QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/4/12 12:25
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(QcEffectivenessDTO.ExportExcelSearchParamDTO dto, HttpServletResponse response);
}
