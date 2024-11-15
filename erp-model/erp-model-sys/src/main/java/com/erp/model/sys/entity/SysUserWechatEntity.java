package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 微信用户表
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_user_wechat")
public class SysUserWechatEntity extends BaseEntity<SysUserWechatEntity> {

    /**
    * sysUid
    */
    @TableField("uid")
    private String uid;
    /**
    * 用于关联同一开放平台下的多个应用的用户
    */
    @TableField("union_id")
    private String unionId;
    /**
    * 微信用户在应用中的唯一标识
    */
    @TableField("open_id")
    private String openId;
    /**
    * 微信名称
    */
    @TableField("nick_name")
    private String nickName;
    /**
    * 微信用户的头像URL
    */
    @TableField("avatar_url")
    private String avatarUrl;
    /**
    * 性别
    */
    @TableField("gender")
    private String gender;
    /**
    * 上次登录时间
    */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;


    public static final String FIELD_UID = "uid";

    public static final String UNION_ID = "union_id";

    public static final String OPEN_ID = "open_id";

    public static final String NICK_NAME = "nick_name";

    public static final String AVATAR_URL = "avatar_url";

    public static final String FIELD_GENDER = "gender";

    public static final String LAST_LOGIN_TIME = "last_login_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}