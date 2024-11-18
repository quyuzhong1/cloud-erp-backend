package com.erp.model.sys.dto;


import com.erp.model.sys.vo.SysMenuVO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SysUserInfo

 * @Date 2022-07-08 17:25
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserDTO implements Serializable {


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

    private Integer userState;
    /**
     * 是否需要修改密码
     */
    private Boolean needChangePwd;

    /**
     * 账号
     */
    private String userAccount;

    private String loginIp;

    private String token;

    private String headIcon;
    /**
     * 是否是超级管理员
     */
    private Boolean isSupper;
    /**
     * 用户类型（默认erp, srm供应商系统）
     */
    private String userType;
    //全局 菜单的列表 后面还会改
    private List<SysMenuVO> overallMenuList;

    //左侧 菜单的列表 后面还会改
    private List<SysMenuVO> leftMenuList;


    //菜单的列表 后面还会改
    private List<String> permissionList;


    //绑定平台
    private String  bindingPlatform;

    //绑定状态
    private Integer bindingState;

}
