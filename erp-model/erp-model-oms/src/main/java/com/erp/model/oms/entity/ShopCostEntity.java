package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 店铺费用表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("shop_cost")
public class ShopCostEntity extends BaseEntity<ShopCostEntity> {

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 平台费率
     */
    @TableField("platform_rate")
    private BigDecimal platformRate;

    /**
     * 平台的选项
     */
    @TableField("dict_platform_option")
    private String dictPlatformOption;

    /**
     * vat 费率
     */
    @TableField("vat_rate")
    private BigDecimal vatRate;

    /**
     * vat 选项
     */
    @TableField("dict_vat_option")
    private String dictVatOption;

    /**
     * 转账费率
     */
    @TableField("transfer_rate")
    private BigDecimal transferRate;

    /**
     * 转帐的选项
     */
    @TableField("dict_transfer_option")
    private String dictTransferOption;


    public static final String SHOP_ID = "shop_id";

    public static final String PLATFORM_RATE = "platform_rate";

    public static final String DICT_PLATFORM_OPTION = "dict_platform_option";

    public static final String VAT_RATE = "vat_rate";

    public static final String DICT_VAT_OPTION = "dict_vat_option";

    public static final String TRANSFER_RATE = "transfer_rate";

    public static final String DICT_TRANSFER_OPTION = "dict_transfer_option";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
