package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 产品采购价格明细表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_product_detail")
public class ScmPurchaseProductDetailEntity extends BaseEntity<ScmPurchaseProductDetailEntity> {

    /**
     * sku 表id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 采购表|采购变更表 id
     */
    @TableField("purchase_id")
    private String purchaseId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 采购交期
     */
    @TableField("delivery_date")
    private Date deliveryDate;

    /**
     * sku 名
     */
    @TableField("sku_name")
    private String skuName;

    /**
     * 区间开始数量
     */
    @TableField("region_start")
    private Integer regionStart;

    /**
     * 区间结束数量
     */
    @TableField("region_end")
    private Integer regionEnd;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 含税单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 最低价
     */
    @TableField("bottom_price")
    private BigDecimal bottomPrice;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private Date effectiveTime;

    /**
     * 失效 时间
     */
    @TableField("lose_effective_time")
    private Date loseEffectiveTime;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 不含税单价
     */
    @TableField("excluding_tax_price ")
    private BigDecimal excludingTaxPrice ;


    public static final String SKU_ID = "sku_id";

    public static final String PURCHASE_ID = "purchase_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_DATE = "delivery_date";

    public static final String SKU_NAME = "sku_name";

    public static final String REGION_START = "region_start";

    public static final String REGION_END = "region_end";

    public static final String CURRENCY = "currency";

    public static final String UNIT_PRICE = "unit_price";

    public static final String BOTTOM_PRICE = "bottom_price";

    public static final String EFFECTIVE_TIME = "effective_time";

    public static final String LOSE_EFFECTIVE_TIME = "lose_effective_time";

    public static final String TAX_RATE = "tax_rate";

    public static final String EXCLUDING_TAX_PRICE  = "excluding_tax_price ";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
