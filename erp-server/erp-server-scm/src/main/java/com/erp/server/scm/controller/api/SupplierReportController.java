package com.erp.server.scm.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierReportDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 供应商报表
 * @CreateTime: 2023-06-12  17:17
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/supplierReport")
public class SupplierReportController extends BaseController {

    /**
     * 供应商报表分页列表
     * @param dto
     * @return
     */
    @PostMapping("paging")
    public ApiResult<PagingVO<SupplierReportDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierReportDTO.PagingSearchParamDTO> dto) {
        return success(null);
    }

    /**
     * 供应商报表导出EXCEL
     * @param dto
     * @param response
     * @return
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody SupplierReportDTO.ExportSearchParamDTO dto, HttpServletResponse response) {

    }

}