package com.cloud.erp.admin.modules.sys.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class SysRoleMenuVO implements Serializable {

    private List<String> selectedMenuIds;

    private List<SysRoleMenuTreeVO> sysRoleMenuTrees;


    private Integer totalMenu;
}
