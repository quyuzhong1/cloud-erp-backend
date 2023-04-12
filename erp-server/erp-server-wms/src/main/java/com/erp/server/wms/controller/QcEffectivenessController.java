package com.erp.server.wms.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.server.wms.service.QcEffectivenessService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 品质时效表
 * @author Will
 * @version 1.0
 * @date 2023/4/12 11:16
 */
@RestController
@RequestMapping("/qcEffectiveness")
public class QcEffectivenessController extends BaseController {

    @Resource
    private QcEffectivenessService qcEffectivenessService;

    /**
     * 质检总览查询
     * @author Will
     * @date: 2023/4/12 11:26
     * @param dto
     * @return ApiResult<List<ViewQcOverviewDTO>>
     */
    @PostMapping("/viewQcOverview")
    public ApiResult<List<QcEffectivenessDTO.ViewQcOverviewDTO>> viewQcOverview(@RequestBody @Validated QcEffectivenessDTO.CommonSearchParamDTO dto) {
        List<QcEffectivenessDTO.ViewQcOverviewDTO> list = qcEffectivenessService.viewQcOverview(dto);
        return success(list);
    }

    /**
     * 质检趋势查询
     * @author Will
     * @date: 2023/4/12 11:26
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcTrend")
    public ApiResult<List<QcEffectivenessDTO.ViewQcTrendDTO>> viewQcTrend(@RequestBody @Validated QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto) {
        List<QcEffectivenessDTO.ViewQcTrendDTO> list = qcEffectivenessService.viewQcTrend(dto);
        return success(list);
    }

    /**
     * 按人员查询
     * @author Will
     * @date: 2023/4/12 11:32
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcForPersonnel")
    public ApiResult<List<QcEffectivenessDTO.ViewQcForPersonnelDTO>> viewQcForPersonnel(@RequestBody @Validated QcEffectivenessDTO.CommonSearchParamDTO dto) {
        List<QcEffectivenessDTO.ViewQcForPersonnelDTO> list = qcEffectivenessService.viewQcForPersonnel(dto);
        return success(list);
    }

    /**
     * 按单据查询
     * @author Will
     * @date: 2023/4/12 11:32
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcForDocument")
    public ApiResult<List<QcEffectivenessDTO.ViewQcForDocumentDTO>> viewQcForDocument(@RequestBody @Validated QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO dto) {
        List<QcEffectivenessDTO.ViewQcForDocumentDTO> list = qcEffectivenessService.viewQcForDocument(dto);
        return success(list);
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/4/12 12:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody QcEffectivenessDTO.ExportExcelSearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = qcEffectivenessService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

}
