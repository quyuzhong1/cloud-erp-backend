package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 用户-店铺权限
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("auth_user_shop")
public class AuthUserShopEntity extends BaseEntity<AuthUserShopEntity> {

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 数据权限(0-全部，1-部分)
    */
    @TableField("data_scope")
    private Integer dataScope;


    public static final String USER_ID = "user_id";

    public static final String SHOP_ID = "shop_id";

    public static final String DATA_SCOPE = "data_scope";

    @Override
    public Serializable pkVal() {
        return null;
    }

}