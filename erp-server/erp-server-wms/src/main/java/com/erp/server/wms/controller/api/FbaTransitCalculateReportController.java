package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.model.wms.dto.FbaTransitCalculateReportDTO;
import com.erp.server.wms.query.FbaTransitCalculateReportQueryHandler;
import com.erp.server.wms.query.FirstMileDeliveryQueryHandler;
import com.erp.server.wms.service.FbaTransitCalculateReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * FBA在途核算报表
 *
 * @author zdy
 * @since 2024-12-12
 */
@Slf4j
@RestController
@LogSystemModule("FBA在途核算报表")
@RequestMapping("/fbaTransitCalculateReport")
public class FbaTransitCalculateReportController extends BaseController {

    @Resource
    private FbaTransitCalculateReportService fbaTransitCalculateReportService;


    /**
     * 列表查询
     * @author zdy
     * @date:  2024-12-12
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "ftcr.warehouse_id",
            menuCode = "wms:fbaTransitCalculateReport:paging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<FbaTransitCalculateReportDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaTransitCalculateReportDTO.PagingParamDTO> dto) {
        return success(fbaTransitCalculateReportService.paging(dto));
    }
    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载FBA期初在途导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        fbaTransitCalculateReportService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入Excel
     * @author zdy
     * @date: 2024/12/16 9:39
     * @param excelImportDTO
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping("/importFile")
    public ApiResult importFile(@ModelAttribute @Validated FbaTransitCalculateReportDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        Boolean result = fbaTransitCalculateReportService.importFile(excelImportDTO.getExcelFile(),response);
        return result ? success() : failure();
    }
    /**
     *  导出Excel
     * @author zdy
     * @date: 2024/12/16 9:39
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody FbaTransitCalculateReportDTO.PagingParamDTO dto) {
        fbaTransitCalculateReportService.exportExcel(dto);
        return success(Boolean.TRUE);
    }

    /**
     * 期末在途调整
     * @param adjustDTO
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "期末在途调整")
    @PostMapping(value = "/adjustTransitQty")
    public ApiResult<Boolean> adjustTransitQty(@RequestBody FbaTransitCalculateReportDTO.AdjustDTO adjustDTO){
        Boolean flag = fbaTransitCalculateReportService.adjustTransitQty(adjustDTO);
        return flag ? success() : failure();
    }
}
