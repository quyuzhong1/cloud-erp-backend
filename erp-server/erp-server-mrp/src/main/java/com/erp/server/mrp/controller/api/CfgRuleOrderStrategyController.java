package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgRuleOrderStrategyDTO;
import com.erp.server.mrp.service.CfgRuleOrderStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 策略（规则设置）
 *
 * @author will
 * @since 2024-10-12
 */
@Slf4j
@RestController
@LogSystemModule("策略（规则设置）")
@RequestMapping("/cfgRuleOrderStrategy")
public class CfgRuleOrderStrategyController extends BaseController {

    @Resource
    private CfgRuleOrderStrategyService cfgRuleOrderStrategyService;

    /**
    * 修改
    * @author will
    * @date:  2024-10-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleOrderStrategyDTO.UpdateDTO dto) {
        cfgRuleOrderStrategyService.update(dto);
        return success();
    }

    /**
     * 查询详情
     * @author will
     * @date 2024/10/12 11:23
     * @param platformType 
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleOrderStrategyDTO.ViewDTO> view(@RequestParam("platformType") String platformType) {
        return success(cfgRuleOrderStrategyService.view(platformType));
    }
}
