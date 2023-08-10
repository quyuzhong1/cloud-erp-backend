package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
  盘点管理-盘点任务
 *
 * @author Lambda
 * @since 2023-07-31
 */
@RestController
@RequestMapping("/stocktakingTask/detail")
public class StocktakingTaskDetailController extends BaseController {

    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    /**
     * 导出盘点任务明细数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        Boolean result = stocktakingTaskDetailService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导入盘点任务明细数据
     */
    @PostMapping("/import")
    public ApiResult exportWarehouse(@RequestParam(value = "mainId") String mainId, @RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = stocktakingTaskDetailService.importFile(mainId,excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载盘点任务明细模板
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        stocktakingTaskDetailService.downloadTemplate(response);
        return success();
    }

    /**
     * 修改盘点数量
     */
    @PostMapping("/update")
    public ApiResult exportWarehouse(@RequestBody @Validated ValidList<StocktakingTaskDetailDTO.UpdateDTO> dto) {
        Boolean result = stocktakingTaskDetailService.updateBatchDetail(dto);
        return result ? success() : failure();
    }
}
