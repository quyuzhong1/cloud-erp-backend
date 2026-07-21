package com.common.business.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Classname LoginUser

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
    /**
     * 是否是超级管理员
     */
    private Boolean isSupper;

    /**
     * 登录所属系统
     * <p>
     * erp：ERP 系统；srm：SRM 供应商系统；pda：PDA 系统。
     * 账号密码登录时写入 Redis，供菜单/权限接口按系统类型过滤数据。
     * </p>
     */
    private String userType;


    //菜单的列表 后面还会改
    private List<String> menuList;

    //菜单的列表 后面还会改
    private List<String> permissionList;

    /**
     * 序列化精简登录用户信息，用于请求头传递
     *
     * @param loginUser 登录用户信息
     * @return URL 编码后的 JSON 字符串
     * @throws UnsupportedEncodingException 编码异常
     */
    public static String simpleLoginUser(LoginUser loginUser) throws UnsupportedEncodingException {
        LoginUser compactUser = new LoginUser();
        compactUser.setUid(loginUser.getUid());
        compactUser.setUserName(loginUser.getUserName());
        compactUser.setRealName(loginUser.getRealName());
        compactUser.setMobile(loginUser.getMobile());
        compactUser.setUserAccount(loginUser.getUserAccount());
        compactUser.setAccessToken(loginUser.getAccessToken());
        compactUser.setBindingPlatform(loginUser.getBindingPlatform());
        compactUser.setIsSupper(loginUser.getIsSupper());
        compactUser.setUserType(loginUser.getUserType());
        return URLEncoder.encode(JSON.toJSONString(compactUser), "UTF-8");
    }
}
