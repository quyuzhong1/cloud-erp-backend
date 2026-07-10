package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysApiTokenDTO;
import com.erp.server.sys.service.SysApiTokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 个人访问令牌
 */
@RestController
@LogSystemModule("个人访问令牌")
@RequestMapping("personalCenter/apiToken")
public class SysApiTokenController extends BaseController {

    @Resource
    private SysApiTokenService sysApiTokenService;

    /**
     * 个人访问令牌列表
     */
    @PostMapping("/list")
    public ApiResult<List<SysApiTokenDTO.ListDTO>> list() {
        return success(sysApiTokenService.listCurrentUserToken());
    }

    /**
     * 新增个人访问令牌
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增个人访问令牌:令牌名称={tokenName},有效期={validityDays}")
    public ApiResult<SysApiTokenDTO.TokenDTO> add(@RequestBody @Validated SysApiTokenDTO.AddDTO dto) {
        return success(sysApiTokenService.add(dto));
    }

    /**
     * 修改个人访问令牌名称
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改个人访问令牌:令牌名称={tokenName}")
    public ApiResult<Boolean> update(@RequestBody @Validated SysApiTokenDTO.UpdateDTO dto) {
        return success(sysApiTokenService.update(dto));
    }

    /**
     * 延期个人访问令牌
     */
    @PostMapping("/extend")
    @LogAction(value = LogActionEnum.UPDATE, desc = "延期个人访问令牌:id={id},有效期={validityDays}")
    public ApiResult<Boolean> extend(@RequestBody @Validated SysApiTokenDTO.ExtendDTO dto) {
        return success(sysApiTokenService.extend(dto));
    }

    /**
     * 删除个人访问令牌
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除个人访问令牌:id={id}")
    public ApiResult<Boolean> remove(@RequestBody @Validated BaseIdDTO dto) {
        return success(sysApiTokenService.removeToken(dto));
    }

    /**
     * 复制个人访问令牌
     */
    @PostMapping("/copy")
    @LogAction(value = LogActionEnum.DOWNLOAD, desc = "复制个人访问令牌:id={id}")
    public ApiResult<SysApiTokenDTO.TokenDTO> copy(@RequestBody @Validated BaseIdDTO dto) {
        return success(sysApiTokenService.copy(dto.getId()));
    }
}
