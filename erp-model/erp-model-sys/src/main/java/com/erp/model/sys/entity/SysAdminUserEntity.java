package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * @Classname SysUserInfoEntity

 * @Date 2022-07-01 16:27
 * @Created by yl
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_admin_user")
public class SysAdminUserEntity extends BaseEntity<SysAdminUserEntity> {

    @TableId(value = "user_id",type =IdType.ASSIGN_ID )
    private String userId;

    //用户名
    @TableField(value = "user_name")
    private String userName;

    //手机号
    @TableField(value = "password")
    private String password;

    @TableField(value = "salt")
    private String salt;

    //用户删除状态 1 正常 0 删除
    @TableField(value = "user_delete_state")
    private Integer userDeleteState;

    //登录的账号
    @TableField(value = "login_account")
    private String loginAccount;

    @TableField(value = "real_name")
    private String realName;

}
