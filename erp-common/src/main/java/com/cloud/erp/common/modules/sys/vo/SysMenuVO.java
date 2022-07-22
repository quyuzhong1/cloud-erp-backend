package com.cloud.erp.common.modules.sys.vo;

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
public class SysMenuVO implements Serializable {


    private String menuId;

    /**
     * 父id
     */
    private String parentId;

    private String parentName;
    /**
     * 菜单图标
     */
    private String menuIcon;
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 授权 (多个用逗号分隔，如：user:list,user:create)
     */
    private String menuCode;


    /**
     * 菜单链接
     */
    private String menuUrl;
    /**
     * 类型 0：目录   1：菜单   2：按钮
     */
    private Integer type;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    @JsonInclude(value= JsonInclude.Include.NON_NULL)
    private List<SysMenuVO> childrenList;
}
