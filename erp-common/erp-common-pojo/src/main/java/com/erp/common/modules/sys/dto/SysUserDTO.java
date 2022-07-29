package com.erp.common.modules.sys.dto;


import com.erp.common.modules.sys.vo.SysMenuVO;
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
     * 账号
     */
    private String userAccount;

    private String loginIp;

    private String token;

    private String headIcon;


    //菜单的列表 后面还会改
    private List<SysMenuVO> menuList;

    //菜单的列表 后面还会改
    private List<String> permissionList;


    //绑定平台
    private String  bindingPlatform;

    //绑定状态
    private Integer bindingState;

}
