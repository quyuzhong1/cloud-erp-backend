package com.erp.model.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SysMenuDTO

 * @Date 2022-07-19 9:32
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class SysMenuDTO  implements Serializable {


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

    /**
     * 菜单排序
     */
    private Integer index;

    /**
     * 所属系统，/api/sys/dictBasic/list?type=menuSystem
     */
    private String system;

    private List<SysMenuDTO> childrenList;


}
