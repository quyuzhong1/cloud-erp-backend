package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysApiTokenWhitelistDTO;
import com.erp.server.sys.service.SysApiTokenWhitelistService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * API Token 接口白名单配置
 */
@RestController
@LogSystemModule("接口白名单配置")
@RequestMapping("apiTokenWhitelist")
public class SysApiTokenWhitelistController extends BaseController {

    @Resource
    private SysApiTokenWhitelistService sysApiTokenWhitelistService;

    /**
     * 接口白名单列表
     */
    @PostMapping("/list")
    public ApiResult<List<SysApiTokenWhitelistDTO.ListDTO>> list() {
        return success(sysApiTokenWhitelistService.listConfig());
    }

    /**
     * 新增接口白名单
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增接口白名单:路径模式={pathPattern}")
    public ApiResult<Boolean> add(@RequestBody @Validated SysApiTokenWhitelistDTO.AddDTO dto) {
        return success(sysApiTokenWhitelistService.add(dto));
    }

    /**
     * 修改接口白名单
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改接口白名单:路径模式={pathPattern}")
    public ApiResult<Boolean> update(@RequestBody @Validated SysApiTokenWhitelistDTO.UpdateDTO dto) {
        return success(sysApiTokenWhitelistService.update(dto));
    }

    /**
     * 移除接口白名单
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "移除接口白名单:id={id}")
    public ApiResult<Boolean> remove(@RequestBody @Validated BaseIdDTO dto) {
        return success(sysApiTokenWhitelistService.removeConfig(dto));
    }
}
