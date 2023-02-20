package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SysUserVO
 * @Description TODO
 * @Date 2022-07-08 16:40
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserJwtVO implements Serializable {

    //token
    private String token;

    //过期时间秒
    private Long expireMinutes;

    //菜单的列表 后面还会改
    private List<String> menuList;

    //菜单的列表 后面还会改
    private List<String> permissionList;
}
