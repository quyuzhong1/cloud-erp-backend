package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @Classname SysMenuDTO
 * @Description TODO
 * @Date 2022-07-19 9:32
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class SysMenuDTO  {


    private String menuId;
    /**
     * 父id
     */
    private String parentId;
    /**
     * 菜单图标
     */
    private String menuIcon;
    /**
     * 菜单名称
     */
    private String menuName;



    /**
     * 菜单链接
     */
    private String menuUrl;
    /**
     * 类型 1：目录   2：菜单   3：按钮  4 功能
     */
    private Integer type;

    private String menuCode;


    private List<SysMenuDTO> childrenList;


}
