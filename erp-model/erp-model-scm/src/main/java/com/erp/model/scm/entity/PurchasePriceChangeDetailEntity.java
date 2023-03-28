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
 * 产品采购变更价 明细表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_price_change_detail")
public class PurchasePriceChangeDetailEntity extends BaseEntity<PurchasePriceChangeDetailEntity> {

    /**
     * sku 表id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 采购价表采购变更表 id
     */
    @TableField("purchase_price_change_id")
    private String purchasePriceChangeId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 采购交期
     */
    @TableField("delivery_date")
    private Integer deliveryDate;

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
     * 生效时间
     */
    @TableField("effective_date")
    private Date effectiveDate;

    /**
     * 失效时间
     */
    @TableField("expire_date")
    private Date expireDate;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 详采购价目详情表
     */
    @TableField("purchase_price_detail_id")
    private BigDecimal purchasePriceDetailId;






    @Override
    public Serializable pkVal() {
        return null;
    }

}
