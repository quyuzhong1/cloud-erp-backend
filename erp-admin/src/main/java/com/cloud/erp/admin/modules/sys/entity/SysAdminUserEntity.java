package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @Classname SysUserInfoEntity
 * @Description TODO
 * @Date 2022-07-01 16:27
 * @Created by yl
 */
@Data
@TableName("sys_admin_user")
public class SysAdminUserEntity {

    @TableId(value = "user_id",type =IdType.ID_WORKER )
    private Long userId;

    //用户名
    private String userName;

    //手机号
    private String mobile;

    private  String password;

    private String salt;

    //用户删除状态 1 正常 0 删除
    @TableLogic
    private Integer userDeleteState;

    //登录的账号
    private String loginAccount;

    //用户状态 1 正常 0 禁用
    private Integer userState;


    @TableField(fill= FieldFill.INSERT)
    private Date createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
