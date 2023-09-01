package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 店铺权限设置表
 * </p>
 *
 * @author Will
 * @since 2023-09-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shop_sys_user_auth")
public class ShopSysUserAuthEntity extends BaseEntity<ShopSysUserAuthEntity> {

    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 授权类型（all全部，part部分）字典shopAuthType
    */
    @TableField("auth_type")
    private String authType;


    public static final String SHOP_ID = "shop_id";

    public static final String USER_ID = "user_id";

    public static final String AUTH_TYPE = "auth_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}