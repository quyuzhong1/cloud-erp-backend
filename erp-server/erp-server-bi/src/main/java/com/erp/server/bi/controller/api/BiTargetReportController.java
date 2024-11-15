package com.erp.server.bi.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.server.bi.service.BiTargetReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

/**
 * 目标相关报表控制层
 * @author Will
 * @version 1.0
 * @date 2023/9/14 12:16
 */
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/report")
public class BiTargetReportController extends BaseController {

    @Resource
    private BiTargetReportService biTargetReportService;

    /**
     * 业绩目标完成
     * @author Will
     * @date: 2023/9/14 16:19
     * @param dto
     * @return ApiResult<LinkedHashMap<Object>>
     */
    @PostMapping("/listTargetFinish")
    public ApiResult<LinkedHashMap<String,Object>> targetFinish(@RequestBody @Validated TargetFinishDTO.ParamDTO dto){
        LinkedHashMap<String,Object> map = biTargetReportService.targetFinish(dto);
        return success(map);
    }

    /**
     * 业绩目标完成导出
     * @author Will
     * @date: 2023/9/19 16:40
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "业绩目标完成导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Void> exportExcel(@RequestBody @Validated TargetFinishDTO.ParamDTO dto, HttpServletResponse response) {
        biTargetReportService.exportExcel(dto, response);
        return success();
    }
}
