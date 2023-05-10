package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 销售订单变更明细
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_change_detail")
public class SoChangeDetailEntity extends BaseEntity<SoChangeDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 原销售数量
     */
    @TableField("old_qty")
    private Integer oldQty;

    /**
     * 销售数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 原单价
     */
    @TableField("old_price")
    private BigDecimal oldPrice;

    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原销售金额
     */
    @TableField("old_amount")
    private BigDecimal oldAmount;

    /**
     * 销售金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 原税率
     */
    @TableField("old_tax_rate")
    private BigDecimal oldTaxRate;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 原币种
     */
    @TableField("old_currency")
    private String oldCurrency;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;

    /**
     * 是否赠品 true 是
     */
    @TableField("is_gift")
    private Boolean isGift;

    /**
     * 是否补发 true 是
     */
    @TableField("is_reissue")
    private Boolean isReissue;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String TYPE = "type";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String OLD_QTY = "old_qty";

    public static final String QTY = "qty";

    public static final String OLD_PRICE = "old_price";

    public static final String PRICE = "price";

    public static final String OLD_AMOUNT = "old_amount";

    public static final String AMOUNT = "amount";

    public static final String OLD_TAX_RATE = "old_tax_rate";

    public static final String TAX_RATE = "tax_rate";

    public static final String OLD_CURRENCY = "old_currency";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String IS_GIFT = "is_gift";

    public static final String IS_REISSUE = "is_reissue";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
