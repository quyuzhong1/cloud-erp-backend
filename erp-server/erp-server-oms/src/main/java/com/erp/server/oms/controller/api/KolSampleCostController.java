package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.server.oms.service.KolSampleCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 寄样费用表
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("寄样费用表")
@RequestMapping("/kolSampleCost")
public class KolSampleCostController extends BaseController {

    @Resource
    private KolSampleCostService kolSampleCostService;

    /**
     * 分页查询
     * @author will
     * @date 2025/12/8 10:18
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<KolSampleCostDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolSampleCostDTO.PagingParamDTO> dto) {
        return success(kolSampleCostService.paging(dto));
    }


    /**
     * 更新费用
     * @author will
     * @date 2025/12/8 10:24
     * @param dto
     * @return ApiResult<Void>
     */
    @PostMapping("/updateCost")
    public ApiResult<Void> updateCost(@RequestBody @Validated KolSampleCostDTO.UpdateCostDTO dto) {
        kolSampleCostService.updateCost(dto);
        return success();
    }


    /**
     * 导出Excel数据
     * @author will
     * @date 2025/12/8 10:28
     * @param dto
     * @param response
     * @return void
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "寄样费用导出Excel数据")
    public void exportList(@RequestBody @Validated KolSampleCostDTO.ExportDTO dto, HttpServletResponse response) {
        kolSampleCostService.exportList(dto, response);
    }


    /**
     * 下载模板
     * @author will
     * @date 2025/12/1 16:23
     * @param response
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/kolSampleCostTemplate.xlsx";
        String standardExcelName = "kolSampleCostTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入Excel数据
     * @author will
     * @date 2025/12/1 16:25
     * @param dto
     * @param response
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入寄样费用单")
    @PostMapping("/import")
    public ApiResult<?> exportWarehouse(@RequestBody BaseDTO.ImportDTO dto, HttpServletResponse response) {
        Boolean result = kolSampleCostService.importFile(dto, response);
        return result ? success() : failure();
    }

}
