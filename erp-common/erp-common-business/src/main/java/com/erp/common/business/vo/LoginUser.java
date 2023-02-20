package com.erp.common.business.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @Classname LoginUser
 * @Description TODO
 * @Date 2022-07-14 18:39
 * @Created by yl
 */
@Data
@NoArgsConstructor
@ToString
public class LoginUser {

    private String uid;
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



    private String accessToken;

    //绑定的平台
    private String bindingPlatform;




    //菜单的列表 后面还会改
    private List<String> menuList;

    //菜单的列表 后面还会改
    private List<String> permissionList;
}
