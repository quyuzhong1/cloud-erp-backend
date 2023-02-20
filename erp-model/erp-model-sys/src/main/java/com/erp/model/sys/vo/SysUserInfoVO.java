package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SysUserInfo
 * @Description TODO
 * @Date 2022-07-08 17:25
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserInfoVO implements Serializable {


    private Long uid;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 电话号码
     */
    private String mobile;


    /**
     * 账号
     */
    private String userAccount;


    //菜单的列表 后面还会改
    private List<String> menuList;

    //菜单的列表 后面还会改
    private List<String> permissionList;

}
