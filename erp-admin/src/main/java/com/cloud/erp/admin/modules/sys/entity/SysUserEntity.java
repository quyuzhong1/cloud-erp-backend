package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Classname SysUserInfoEntity
 * @Description TODO
 * @Date 2022-07-01 16:27
 * @Created by yl
 */
@Data
@TableName("sys_user")
public class SysUserEntity {

    @TableId
    private Long userId;

    //用户名
    private String userName;

    //手机号
    private String mobile;

    private  String password;

    private String salt;

    //用户删除状态 1 正常 0 删除
    private Integer userDeleteState;

    //登录的账号
    private String loginAccount;

    //用户状态 1 正常 0 禁用
    private Integer userState;

    private Date createTime;

    private Date updateTime;
}
