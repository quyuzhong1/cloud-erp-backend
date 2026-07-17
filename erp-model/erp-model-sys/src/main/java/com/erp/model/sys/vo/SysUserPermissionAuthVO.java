package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户按钮权限编码响应 VO
 * <p>
 * 用于登录后独立获取按钮权限，返回当前用户拥有的所有按钮 menuCode 列表。
 * </p>
 *
 * @Classname SysUserPermissionAuthVO
 * @Date 2026-06-18
 */
@NoArgsConstructor
@Data
public class SysUserPermissionAuthVO implements Serializable {

    /**
     * 按钮权限编码列表
     */
    private List<String> permissionList;
}
