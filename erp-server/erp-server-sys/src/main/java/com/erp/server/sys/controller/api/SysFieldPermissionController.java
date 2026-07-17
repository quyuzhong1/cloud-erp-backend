package com.erp.server.sys.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysFieldPermissionDTO;
import com.erp.server.sys.service.SysFieldPermissionService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段权限管理 控制器（"角色管理 - 字段权限"tab 后端）
 *
 * <p>职责：维护"角色 × 字段权限菜单（sys_menu.type=5）"的可见关系，
 * 不直接操作 cfg_mask_field，也不接触功能/按钮权限。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@RestController
@LogSystemModule("字段权限管理")
@RequestMapping("/fieldPermission")
public class SysFieldPermissionController extends BaseController {

    @Resource
    private SysFieldPermissionService sysFieldPermissionService;

    /**
     * 列表：当前角色对所有字段权限菜单的"可见/不可见"状态
     */
    @PostMapping("/list")
    public ApiResult<List<SysFieldPermissionDTO.ListVO>> list(
            @RequestBody @Validated SysFieldPermissionDTO.ListSearchDTO dto) {
        return success(sysFieldPermissionService.list(dto));
    }

    /**
     * 保存：全量替换该角色"字段权限"维度的关联（不影响功能/按钮/数据权限）
     */
    @PostMapping("/save")
    @LogAction(value = LogActionEnum.UPDATE, desc = "保存角色字段权限")
    public ApiResult<Boolean> save(@RequestBody @Validated SysFieldPermissionDTO.SaveDTO dto) {
        return success(sysFieldPermissionService.save(dto));
    }
}
