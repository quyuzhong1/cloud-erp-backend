package com.cloud.erp.admin.modules.sys.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname SysMenuVO
 * @Description TODO
 * @Date 2022-07-19 10:39
 * @Created by yl
 */
@NoArgsConstructor
@Data
@ToString
public class SysRoleMenuTreeVO implements Serializable {

    private String menuId;

    private String menuCode;

    private String menuName;

    private String parentId;

    private String parentName;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 菜单链接
     */
    private String menuUrl;

    /**
     * 类型 1：目录   2：菜单   3：按钮  4：功能
     */
    private Integer type;

    //选中状态 0 没有  1 有
    private Boolean selectState;


    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<SysRoleMenuTreeVO> childrenList;
}
