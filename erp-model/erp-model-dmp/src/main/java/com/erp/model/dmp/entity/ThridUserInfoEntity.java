package com.erp.model.dmp.entity;

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
 * 用户表
 * </p>
 *
 * @author jack
 * @since 2025-04-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("thrid_user_info")
public class ThridUserInfoEntity extends BaseEntity<ThridUserInfoEntity> {

    /**
    * unionId
    */
    @TableField("unionid")
    private String unionid;
    /**
    * openId
    */
    @TableField("openid")
    private String openid;
    /**
    * 用户昵称
    */
    @TableField("nick_name")
    private String nickName;
    /**
    * 用户头像图片的 URL
    */
    @TableField("avatar_url")
    private String avatarUrl;
    /**
    * 用户性别
    */
    @TableField("gender")
    private Integer gender;
    /**
    * 姓名
    */
    @TableField("username")
    private String username;
    /**
    * 手机号码
    */
    @TableField("phone_number")
    private String phoneNumber;
    /**
    * 所在国家
    */
    @TableField("country")
    private String country;
    /**
    * 所在省份
    */
    @TableField("province")
    private String province;
    /**
    * 所在城市
    */
    @TableField("city")
    private String city;
    /**
    * 显示 country，province，city 所用的语言,强制返回 “zh_CN”
    */
    @TableField("language")
    private String language;
    /**
    * 最近登录时间
    */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    /**
    * 用户状态
    */
    @TableField("status")
    private String status;
    /**
    * 用户类型
    */
    @TableField("type")
    private String type;


    public static final String UNIONID = "unionid";

    public static final String OPENID = "openid";

    public static final String NICK_NAME = "nick_name";

    public static final String AVATAR_URL = "avatar_url";

    public static final String GENDER = "gender";

    public static final String USERNAME = "username";

    public static final String PHONE_NUMBER = "phone_number";

    public static final String COUNTRY = "country";

    public static final String PROVINCE = "province";

    public static final String CITY = "city";

    public static final String LANGUAGE = "language";

    public static final String LAST_LOGIN_TIME = "last_login_time";

    public static final String STATUS = "status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}