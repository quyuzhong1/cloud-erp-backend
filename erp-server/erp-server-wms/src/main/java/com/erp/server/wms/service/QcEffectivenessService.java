package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcEffectivenessDTO;

import java.util.List;

/**
 * @author Will
 * @version 1.0

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
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/4/12 12:25
     */
    Boolean exportExcel(QcEffectivenessDTO.ExportExcelSearchParamDTO dto);

    /**
     * @description: 查询各个状态的质检单数量
     * @author Will
     * @date: 2024/5/6 18:12
     * @param dto
     * @return List<ViewQcOverviewDetailDTO>
     */
    List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> listQcBillGroupQcStatus(QcEffectivenessDTO.CommonSearchParamDTO dto);

    PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> exportQcEffectivenessDocument(PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto);

    PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> exportQcEffectivenessPersonnel(PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto);
}
