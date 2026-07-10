package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户菜单权限响应 VO
 * <p>
 * 用于登录后独立获取菜单权限，包含左侧导航菜单树和全量菜单树。
 * </p>
 *
 * @Classname SysUserMenuAuthVO
 * @Date 2026-06-18
 */
@NoArgsConstructor
@Data
public class SysUserMenuAuthVO implements Serializable {

    /**
     * 左侧导航菜单树（不含按钮节点）
     */
    private List<SysMenuVO> leftMenuList;

    /**
     * 全量菜单树（含目录、菜单、按钮节点）
     */
    private List<SysMenuVO> overallMenuList;
}
