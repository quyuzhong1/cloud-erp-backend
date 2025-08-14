package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.server.mrp.service.CfgRuleExpireTimeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 时效配置表 前端控制器
 *
 * @author liaohui
 * @since 2025-02-13
 */
@RestController
@LogSystemModule("时效（规则设置）")
@RequestMapping("/cfgRuleExpireTime")
public class CfgRuleExpireTimeController extends BaseController {

    @Resource
    private CfgRuleExpireTimeService cfgRuleExpireTimeService;


    /**
     * 修改
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "规则修改")
    public ApiResult<String> update(@RequestBody @Validated CfgRuleExpireTimeDTO.UpdateDTO dto) {
        cfgRuleExpireTimeService.update(dto);
        return success();
    }

    /**
     * 查看详情
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<CfgRuleExpireTimeDTO.ViewDTO> view() {
        CfgRuleExpireTimeDTO.ViewDTO view = cfgRuleExpireTimeService.view();
        return success(view);
    }

}
