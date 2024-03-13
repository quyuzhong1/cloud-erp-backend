package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.CfgSettingService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.CfgSettingDTO;

/**
 * 系统配置管理
 *
 * @author zdy
 * @since 2024-02-29
 */
@Slf4j
@RestController
@LogSystemModule("系统配置管理")
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2024-02-29
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingDTO.AddDTO dto) {
        return success(cfgSettingService.add(dto));
    }

    /**
     * 查询配置
     *
     * @return ApiResult
     * @author zdy
     * @date: 2024-01-08
     */
    @GetMapping("/view")
    @LogAction(value = LogActionEnum.UPDATE, desc = "系统配置管理修改")
    public ApiResult<CfgSettingDTO.ViewDTO> view() {
        CfgSettingDTO.ViewDTO view = cfgSettingService.view();
        return success(view);
    }

}
