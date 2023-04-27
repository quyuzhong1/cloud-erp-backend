package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.model.wms.entity.QcInfoEntity;
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
public interface QcInfoMapper extends BaseMapper<QcInfoEntity> {
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

    /**
     * 分页获取
     * @param query
     * @param params
     * @return
     */
    IPage<QcInfoDTO.PagingViewDTO> paging(Page query, @Param("params") QcInfoDTO.PagingParamDTO params);

    
    /**
     * 获取导出信息
     * @author yl
     * @date 2023-04-19 18:44
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.QcBillDTO.PagingViewDTO>
     */
    List<QcInfoDTO.PagingViewDTO> getExport(@Param("params") QcInfoDTO.ExportDTO dto);

    /**
     * 获取下推数据
     * @author yl
     * @date 2023-04-23 12:12
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(@Param("ids")List<String> ids);
}
