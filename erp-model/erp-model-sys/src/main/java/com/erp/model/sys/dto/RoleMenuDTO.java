package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname 角色菜单
 * @Description TODO
 * @Date 2022-07-19 17:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RoleMenuDTO implements Serializable {

    private List<String> selectedMenuIds;

    private List<RoleMenuTreeDTO> sysRoleMenuTrees;


    private Integer totalMenu;
}
