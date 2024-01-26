package com.erp.model.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname SysLoginUserVO

 * @Date 2022-07-25 15:25
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class SysLoginUserVO implements Serializable {

    private String accessToken;

    private List<SysMenuVO> overallMenuList;

    private List<SysMenuVO> leftMenuList;


    private List<String> permissionList;

    private String userId;
    private String userName;
    private String headIcon;

    //0 未绑定  1  已绑定
    private Integer bindingState;

    private String bindingPlatform;
    /**
     * 是否需要修改密码
     */
    private Boolean needChangePwd;

    /**
     * 是否是超级管理员
     */
    private Boolean isSupper;
}
