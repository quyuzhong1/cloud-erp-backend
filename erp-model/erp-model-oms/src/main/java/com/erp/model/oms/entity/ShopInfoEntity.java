package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * 店铺表
 *
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("shop_info")
public class ShopInfoEntity extends BaseEntity<ShopInfoEntity> {

    /**
     * 平台的dict值
     */
    @TableField("platform_dict")
    private String platformDict;

    /**
     * 编号
     */
    @TableField("shop_code")
    private String shopCode;

    /**
     * 店铺名称
     */
    @TableField("name")
    private String name;

    /**
     * 客户的code
     */
    @TableField("customer_code")
    private String customerCode;


    public static final String PLATFORM_DICT = "platform_dict";

    public static final String SHOP_CODE = "shop_code";

    public static final String NAME = "name";

    public static final String CUSTOMER_CODE = "customer_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
