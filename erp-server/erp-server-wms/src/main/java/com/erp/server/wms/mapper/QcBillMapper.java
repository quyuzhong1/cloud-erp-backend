package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.entity.QcBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 质检单表 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Mapper
public interface QcBillMapper extends BaseMapper<QcBillEntity> {
    /**
     * @description: 质检总览查询
     * @author Will
     * @date: 2023/4/17 18:35
     * @param params
     * @return List<ViewQcOverviewDetailDTO>
     */
    List<QcEffectivenessDTO.ViewQcOverviewDetailDTO> listQcBillGroupQcStatus(@Param("params") QcEffectivenessDTO.CommonSearchParamDTO params);
    /**
     * @description: 按日期查询质检趋势
     * @author Will
     * @date: 2023/4/18 12:31
     * @param beginDate
     * @param endDate 
     * @return List<GroupQcTrendDTO> 
     */
    List<QcEffectivenessDTO.GroupQcTrendDTO> listQcBillGroupQcTrend(@Param("type")String type, @Param("beginDate")LocalDate beginDate,@Param("endDate") LocalDate endDate);
    /**
     * @description: 按人员查询
     * @author Will
     * @date: 2023/4/18 19:37
     * @param query
     * @param params
     * @return IPage<ViewQcForPersonnelDTO>
     */
    IPage<QcEffectivenessDTO.ViewQcForPersonnelDTO> viewQcForPersonnel(Page query,@Param("params") QcEffectivenessDTO.CommonSearchParamDTO params);
    /**
     * @description: 按单据查询
     * @author Will
     * @date: 2023/4/18 19:38
     * @param query
     * @param params
     * @return IPage<ViewQcForDocumentDTO>
     */
    IPage<QcEffectivenessDTO.ViewQcForDocumentDTO> viewQcForDocument(Page query,@Param("params") QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO params);
    /**
     * @description: 按人员查询导出
     * @author Will
     * @date: 2023/4/18 20:21
     * @param params
     * @return List<ViewQcForPersonnelDTO>
     */
    List<QcEffectivenessDTO.ViewQcForPersonnelDTO> viewExportQcForPersonnel(@Param("params") QcEffectivenessDTO.ExportExcelSearchParamDTO params);
    /**
     * @description: 按人员单据导出
     * @author Will
     * @date: 2023/4/18 20:21
     * @param params
     * @return List<ViewQcForPersonnelDTO>
     */
    List<QcEffectivenessDTO.ViewQcForDocumentDTO>viewExportQcForDocument(@Param("params") QcEffectivenessDTO.ExportExcelSearchParamDTO params);
}
