package com.erp.server.mrp.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销量试算表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoDimService extends SuperService<CalcSalesInfoDimEntity> {


    /**
     *
     * @param calcResultList 计算参数
     */
    void calcSalesInfo(List<CalcSalesInfoDimDTO.CalcResultDTO> calcResultList);

    /**
     * 分页
     * @param params 参数
     */
    PagingVO<CalcSalesInfoDimDTO.PagingView> paging(PagingDTO<CalcSalesInfoDimDTO.PagingParamDTO> params);

    /**
     * 详情
     * @param id id
     */
    CalcSalesInfoDimDTO.ViewDTO view(String id);

    /**
     * 历史销量
     * @param dto 参数
     */
    CalcSalesInfoDimDTO.HistorySalesVO historySales(CalcSalesInfoDimDTO.HistorySalesDTO dto);


    /**
     * 日销量预估
     * @param dto 参数
     */
    CalcSalesInfoDimDTO.SalesEstimateDTO salesEstimation(CalcSalesInfoDimDTO.HistorySalesDTO dto);

    /**
     * 导出
     * @param dto 参数
     */
    void exportSalesInfo(CalcSalesInfoDimDTO.ExportSalesInfoDTO dto);

    /**
     * 导出
     * @param dto 参数
     */
    PagingVO<CalcSalesInfoDimDTO.ExportResultDTO> getListExportData(PagingDTO<CalcSalesInfoDimDTO.ExportSalesInfoDTO> dto);

    /**
     * 试算详情
     * @param params 参数
     */
    PagingVO<CalcSalesInfoDimDTO.DetailViewDTO> pagingDetail(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params);

    /**
     * 试算模板
     * @param params 参数
     */
    PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO> pagingTemplate(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params);

    /**
     * 修改备注
     * @param id 试算id
     * @param remark 备注
     */
    BatchResultDTO updateRemark(String id, String remark);

    /**
     * 下载系统销量
     *
     * @param calcSalesInfoDimId 参数
     */
    void downloadHistorySales(String calcSalesInfoDimId, HttpServletResponse response);


}
