package com.erp.server.sys.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgExportFieldDTO;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.server.sys.service.CfgExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 导出字段配置
 */
@Slf4j
@RestController
@LogSystemModule("查询条件配置表")
@RequestMapping("/export")
public class CfgExportController extends BaseController {

    @Resource
    private CfgExportService cfgExportService;

    /**
     * 获取导出字段
     */
    @GetMapping("/getExportField")
    public ApiResult<List<CfgExportFieldDTO>> getExportField(@RequestParam(value = "exportName") String exportName) {
        return success(cfgExportService.getExportField(exportName));
    }
}
