package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.server.mrp.service.CfgRuleCalcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 试算配置
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("试算配置")
@RequestMapping("/cfgRuleCalc")
public class CfgRuleCalcController extends BaseController {

    @Resource
    private CfgRuleCalcService cfgRuleCalcService;

    /**
    * 新增
    * @author liaohui
    * 2024-11-11
    * @param dto 参数
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "试算配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgRuleCalcDTO.AddDTO dto, HttpServletResponse response) {
        return success(cfgRuleCalcService.add(dto, response));
    }


    /**
     * 下载历史销量
     * @param dto 参数
     */
    @PostMapping("/downloadHistorySales")
    public ApiResult<String> downloadHistorySales(@RequestBody @Validated CfgRuleCalcDTO.DownloadDTO dto) {
        cfgRuleCalcService.downloadHistorySales(dto);
        return success();
    }

    /**
     * 导入模板
     * @param response 参数
     * @return ApiResult<?>
     */
    @GetMapping("/downloadRuleTemplate")
    public ApiResult<?> downloadRuleTemplate(HttpServletResponse response) {
        cfgRuleCalcService.downloadRuleTemplate(response);
        return success();
    }

}
