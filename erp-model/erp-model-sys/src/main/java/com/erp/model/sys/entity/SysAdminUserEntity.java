package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Classname SysUserInfoEntity
 * @Date 2022-07-01 16:27
 * @Created by yl
 */
@Data
@TableName("sys_admin_user")
public class SysAdminUserEntity implements Serializable {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
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

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除字段
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;

}
