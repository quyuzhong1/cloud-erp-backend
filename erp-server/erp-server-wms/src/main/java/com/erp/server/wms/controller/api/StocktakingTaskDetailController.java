package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.server.wms.query.StocktakingTaskQueryHandler;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
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
@LogSystemModule("盘点任务")
@RequestMapping("/stocktakingTask/detail")
public class StocktakingTaskDetailController extends BaseController {

    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;

    /**
     * 导出盘点任务明细数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出盘点任务明细")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody StocktakingTaskDTO.BaseIdDTO dto) {
        Boolean result = stocktakingTaskDetailService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 导入盘点任务明细数据
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入盘点任务明细")
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改盘点数量:id={id} 盘点数量:qty={qty}")
    @PostMapping("/update")
    public ApiResult exportWarehouse(@RequestBody @Validated ValidList<StocktakingTaskDetailDTO.UpdateDTO> dto) {
        Boolean result = stocktakingTaskDetailService.updateBatchDetail(dto);
        return result ? success() : failure();
    }
}
