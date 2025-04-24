package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author Lambda
 * @since 2023-03-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price_history")
public class PurchasePriceHistoryEntity extends BaseEntity<PurchasePriceHistoryEntity> {

    /**
     * sku 表id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 采购价表 id
     */
    @TableField("purchase_price_id")
    private String purchasePriceId;


    /**
     * 采购价详情表 id
     */
    @TableField("price_detail_id")
    private String priceDetailId;



    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * sku 名
     */
    @TableField("product_name")
    private String productName;

    /**
     * 区间开始数量
     */
    @TableField("min_qty")
    private Integer minQty;

    /**
     * 区间结束数量
     */
    @TableField("max_qty")
    private Integer maxQty;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 含税单价
     */
    @TableField("tax_price")
    private BigDecimal taxPrice;

    /**
     * 失效时间
     */
    @TableField("expire_date")
    private LocalDate expireDate;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 交期
     */
    @TableField("delivery_day")
    private Integer deliveryDay;

    /**
     * 生效时间
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     *变更详情id
     */
    @TableField("change_detail_id")
    private String changeDetailId;


    public static final String SKU_ID = "sku_id";

    public static final String PURCHASE_PRICE_ID = "purchase_price_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String MIN_QTY = "min_qty";

    public static final String MAX_QTY = "max_qty";

    public static final String FIELD_CURRENCY = "currency";

    public static final String TAX_PRICE = "tax_price";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String TAX_RATE = "tax_rate";

    public static final String DELIVERY_DATE = "delivery_date";

    public static final String EFFECTIVE_DATE = "effective_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
