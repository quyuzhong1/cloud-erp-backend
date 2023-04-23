package com.erp.server.wms.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.wms.dto.QcReportDetailDTO;
import com.erp.server.wms.service.QcReportDetailService;
import com.erp.server.wms.service.QcReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 质检单
 *
 * @author lambda
 * @since 2023-04-14
 */
@RestController
@RequestMapping("/qcReportDetail")
public class QcBillReportDetailController extends BaseController {

    @Resource
    private QcReportService qcReportService;

    @Resource
    private QcReportDetailService qcReportDetailService;


    /**
     * 导出质检报告
     *
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/exportQcReport")
    public ApiResult exportQcReport(@RequestBody @Valid QcReportDetailDTO.ExportDTO dto, HttpServletResponse response) {
        qcReportService.exportQcReport(dto, response);
        return success();
    }


    /**
     * 导入质检报告
     *
     * @param response
     * @return
     */
    @PostMapping("/importFile")
    public ApiResult<QcReportDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.QcReportDetailExcelImportDTO excelImportDTO, HttpServletResponse response) {
        QcReportDetailDTO.ImportDTO result = qcReportDetailService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getQcType(),response);
        return success(result);
    }

}
