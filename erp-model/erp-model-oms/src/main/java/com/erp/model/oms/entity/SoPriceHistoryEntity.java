package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_price_history")
public class SoPriceHistoryEntity extends BaseEntity<SoPriceHistoryEntity> {

    /**
    * sku 表id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 销售价表 id
    */
    @TableField("so_price_id")
    private String soPriceId;
    /**
    * sku no
    */
    @TableField("sku_no")
    private String skuNo;
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
    private LocalDateTime expireDate;
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
    * 销售价目详情表id
    */
    @TableField("price_detail_id")
    private String priceDetailId;
    /**
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 销售价目变更详情id
    */
    @TableField("change_detail_id")
    private String changeDetailId;


    public static final String SKU_ID = "sku_id";

    public static final String SO_PRICE_ID = "so_price_id";

    public static final String SKU_NO = "sku_no";

    public static final String MIN_QTY = "min_qty";

    public static final String MAX_QTY = "max_qty";

    public static final String CURRENCY = "currency";

    public static final String TAX_PRICE = "tax_price";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String TAX_RATE = "tax_rate";

    public static final String DELIVERY_DAY = "delivery_day";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String PRICE_DETAIL_ID = "price_detail_id";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String CHANGE_DETAIL_ID = "change_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}