package com.erp.model.oms.entity;

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
 * 店铺授权表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shop_auth")
public class ShopAuthEntity extends BaseEntity<ShopAuthEntity> {

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 店铺登陆token
     */
    @TableField("token")
    private String token;
    /**
     * 刷新的token
     */
    @TableField("access_token")
    private String accessToken;
    /**
     * 访问令牌过期之前的秒数。
     */
    @TableField("expires_in")
    private Integer expiresIn;
    /**
     * 对应 dmp 表id
     */
    @TableField("app_client_id")
    private String appClientId;

    private String type;
    /**
     * 刷新token
     */
    @TableField("refresh_token")
    private String refreshToken;
    /**
     * 虾皮授权店铺id
     */
    @TableField("shopee_id")
    private String shopeeId;
    /**
     * token失效时间
     */
    @TableField("token_expire_time")
    private LocalDateTime tokenExpireTime;
    /**
     * 刷新token状态 0 成功 1失败
     */
    @TableField("refresh_status")
    private Integer refreshStatus;
    /**
     * 刷新token错误信息
     */
    @TableField("refresh_error_msg")
    private String refreshErrorMsg;

    @TableField(exist = false)
    private String extendData;

    @TableField(exist = false)
    private String areaCode;

    @TableField(exist = false)
    private String dictPlatform;
    @TableField(exist = false)
    private String shopName;

    public static final String SHOP_ID = "shop_id";

    public static final String TOKEN = "token";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String EXPIRED_TIME = "expired_time";

    public static final String APP_CLIENT_ID = "app_client_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}