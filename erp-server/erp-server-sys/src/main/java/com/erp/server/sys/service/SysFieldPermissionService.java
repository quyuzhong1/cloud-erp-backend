package com.erp.server.sys.service;

import com.erp.model.sys.dto.SysFieldPermissionDTO;

import java.util.List;

/**
 * 字段权限管理服务（"角色管理 - 字段权限"tab 后端）
 *
 * <p>纯门面：聚合 {@code sys_menu(type=5)} + {@code sys_role_menu} + {@code cfg_mask_field}，
 * 不引入新表。</p>
 *
 * @author cloud-erp
 */
public interface SysFieldPermissionService {

    /**
     * 列表：当前角色对所有字段权限菜单的"可见/不可见"状态
     */
    List<SysFieldPermissionDTO.ListVO> list(SysFieldPermissionDTO.ListSearchDTO dto);

    /**
     * 保存：全量替换该角色"字段权限"维度的关联（不影响其它 type 的菜单关系）
     */
    Boolean save(SysFieldPermissionDTO.SaveDTO dto);
}
