package com.erp.server.oms.controller.api;


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
import com.erp.server.oms.service.CfgSettingService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfgSettingDTO;

/**
 * 系统配置管理
 *
 * @author zdy
 * @since 2025-03-24
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
    * @author zdy
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingDTO.AddDTO dto) {
        return success(cfgSettingService.add(dto));
    }

    /**
     * 查询配置
     * @author will
     * @date:  2024-01-08
     * @return ApiResult
     */
    @GetMapping("/view")
    @LogAction(value = LogActionEnum.UPDATE, desc = "系统配置管理修改")
    public ApiResult<CfgSettingDTO.ViewDTO> view() {
        CfgSettingDTO.ViewDTO view = cfgSettingService.view();
        return success(view);
    }
    /**
     * 获取配置
     * key= timeOutConfig 超时配置
     * @return
     */
    @GetMapping("/getSetting")
    public ApiResult<CfgSettingDTO.ViewDTO> getTimeOutSetting(@RequestParam(value = "key") String key) {
        return success(cfgSettingService.getSetting(key));
    }
}
