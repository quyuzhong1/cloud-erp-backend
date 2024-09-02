package com.erp.server.scm.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.SupplierReportDTO;
import com.erp.server.scm.service.SupplierReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商报表
 * @CreateTime: 2023-06-12  17:17
 * @Author: zhangchunlin
 */
@RestController
@LogSystemModule("WMS供应商报表")
@RequestMapping(value = "/supplierReport")
public class SupplierReportController extends BaseController {

    @Autowired
    private SupplierReportService supplierReportService;

    /**
     * 供应商报表分页列表
     * @param dto
     * @return
     */
    @PostMapping("paging")
    public ApiResult<PagingVO<SupplierReportDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierReportDTO.PagingSearchParamDTO> dto) {
        return success(supplierReportService.supplierPaging(dto));
    }

    /**
     * 供应商报表导出EXCEL
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "供应商报表导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody SupplierReportDTO.ExportSearchParamDTO dto) {
        supplierReportService.exportList(dto);
        return success(true);
    }

}