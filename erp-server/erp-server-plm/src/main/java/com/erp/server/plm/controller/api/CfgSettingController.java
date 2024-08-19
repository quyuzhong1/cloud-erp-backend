package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.PlmCfgSettingDTO;
import com.erp.server.plm.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 系统配置管理
 *
 * @author lrp
 * @since 2024-07-25
 */
@Slf4j
@RestController
@LogSystemModule("系统配置管理")
@RequestMapping("/cfgSetting")
public class CfgSettingController extends BaseController {

    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 新增或更新
     * @author lrp
     * @date:  2024-06-28
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/addOrUpdate")
    @LogAction(value = LogActionEnum.INSERT, desc = "系统配置新增或更新")
    public ApiResult<Boolean> addOrUpdate(@RequestBody @Validated PlmCfgSettingDTO.CommonDTO dto) {
        return success(cfgSettingService.addOrUpdate(dto));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-06-28
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<PlmCfgSettingDTO.CommonDTO> view() {
        return success(cfgSettingService.view());
    }
}
