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
 * 产品采购价格明细表
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price_detail")
public class PurchasePriceDetailEntity extends BaseEntity<PurchasePriceDetailEntity> {

    /**
     * sku 表id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 采购价id
     */
    @TableField("purchase_price_id")
    private String purchasePriceId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 采购交期
     */
    @TableField("delivery_day")
    private Integer deliveryDay;

    /**
     * sku 名
     */
    @TableField("product_name")
    private String productName;

    /**
     * 最小数量
     */
    @TableField("min_qty")
    private Integer minQty;

    /**
     * 最大数量
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
     * 生效时间
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;


    /**
     * true 禁用
     * false 启用
     * 默认false
     */
    @TableField("disabled")
    private Boolean disabled;


    @Override
    public Serializable pkVal() {
        return null;
    }

}
