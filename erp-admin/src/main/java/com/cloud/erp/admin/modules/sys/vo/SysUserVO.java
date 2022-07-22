package com.cloud.erp.admin.modules.sys.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Classname SysUserVO
 * @Description TODO
 * @Date 2022-07-12 17:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysUserVO implements Serializable {

    private String flagId;

    private String userId;


    private String userName;

    //用户状态 1 正常  0 禁用
    private String userState;


    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话号码
     */
    private String mobile;

    //是否一添加 1 已添加  0  没有
    private Integer flagState;



}
