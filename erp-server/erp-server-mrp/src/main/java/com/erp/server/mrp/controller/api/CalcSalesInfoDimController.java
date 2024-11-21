package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 销量试算表
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("销量试算表")
@RequestMapping("/calcSalesInfoDim")
public class CalcSalesInfoDimController extends BaseController {

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;


    /**
     * 销量试算列表
     * @param params 参数
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.PagingParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.PagingView> paging = calcSalesInfoDimService.paging(params);
        return success(paging);
    }

    /**
     * 销量试算明细
     * @param id id
     */
    @GetMapping("/view")
    public ApiResult<CalcSalesInfoDimDTO.ViewDTO> view(@RequestParam String id) {
        CalcSalesInfoDimDTO.ViewDTO view = calcSalesInfoDimService.view(id);
        return success(view);
    }

    /**
     * 历史库存
     * @param dto 参数
     */
    @PostMapping("/historySales")
    public ApiResult<CalcSalesInfoDimDTO.HistorySalesVO> historySales(@RequestBody @Validated CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimDTO.HistorySalesVO historySales = calcSalesInfoDimService.historySales(dto);
        return success(historySales);
    }

    /**
     * 日销量预估
     * @param dto 参数
     */
    @PostMapping("/salesEstimation")
    public ApiResult<CalcSalesInfoDimDTO.SalesEstimateDTO> salesEstimation(@RequestBody @Validated CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimDTO.SalesEstimateDTO result = calcSalesInfoDimService.salesEstimation(dto);
        return success(result);
    }


    /**
     * 导出
     */
    @PostMapping("/exportSalesInfo")
    public ApiResult<String> exportSalesInfo(@RequestBody CalcSalesInfoDimDTO.ExportSalesInfoDTO dto) {
        calcSalesInfoDimService.exportSalesInfo(dto);
       return success();
    }

    /**
     *
     * 试算跟踪明细
     * @param params 参数
     */
    @PostMapping("/pagingDetail")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.DetailViewDTO>> pagingDetail(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.DetailViewDTO> page = calcSalesInfoDimService.pagingDetail(params);
        return success(page);
    }


    /**
     *
     * 试算跟踪明细
     * @param params 参数
     */
    @PostMapping("/pagingTemplate")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO>> pagingTemplate(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO> page = calcSalesInfoDimService.pagingTemplate(params);
        return success(page);
    }

}
