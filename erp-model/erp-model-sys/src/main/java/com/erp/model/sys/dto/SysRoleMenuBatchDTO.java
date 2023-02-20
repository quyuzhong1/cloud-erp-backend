package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @Classname SysRoleMenuBatchDTO
 * @Description TODO
 * @Date 2022-07-20 9:14
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysRoleMenuBatchDTO implements Serializable {
    /**
     * 角色id
     */
    private String roleId;

    /**
     * 选中的菜单权限集合
     */
    private Set<SysRoleMenuDataScopeDTO> menuIdList;
}
