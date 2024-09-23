package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.CfgSettingDTO;
import com.erp.model.wms.dto.CfgSettingVirtualDTO;
import com.erp.server.wms.service.CfgSettingService;
import com.erp.server.wms.service.CfgSettingVirtualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 系统配置管理
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓设置")
@RequestMapping("/cfgSettingVirtual")
public class CfgSettingVirtualController extends BaseController{

    @Resource
    private CfgSettingVirtualService cfgSettingVirtualService;

    /**
     * 虚拟仓设置新增
     * @author will
     * @date 2024/9/23 15:59
     * @param dto
     * @return ApiResult<AddDTO>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgSettingVirtualDTO.AddDTO dto) {
        return success(cfgSettingVirtualService.addVirtual(dto));
    }

    /**
     * 虚拟仓设置查看
     * @author will
     * @date 2024/9/23 15:59
     * @return ApiResult<AddDTO>
     */
    @GetMapping("/view")
    public ApiResult<CfgSettingVirtualDTO.ViewDTO> view() {
        CfgSettingVirtualDTO.ViewDTO view = cfgSettingVirtualService.viewVirtual();
        return success(view);
    }
}
