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


/**
 * <p>
 * 销售价目表明细
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_price_detail")
public class SoPriceDetailEntity extends BaseEntity<SoPriceDetailEntity> {

    /**
    * sku 表id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
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
    private LocalDate expireDate;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 生效时间
    */
    @TableField("effective_date")
    private LocalDate effectiveDate;
    /**
    * 禁用状态 true 禁用 false 启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    /**
     *  主单单据状态
     */
    @TableField(exist = false)
    private String approveStatus;

    /**
     * 客户id
     */
    @TableField(exist = false)
    private String customerId;

    /**
     * 价目表编码
     */
    @TableField(exist = false)
    private String priceCode;

    /**
     * 销售组织
     */
    @TableField(exist = false)
    private String soOrgName;


    public static final String SKU_ID = "sku_id";

    public static final String MAIN_ID = "main_id";

    public static final String SKU_NO = "sku_no";

    public static final String MIN_QTY = "min_qty";

    public static final String MAX_QTY = "max_qty";

    public static final String CURRENCY = "currency";

    public static final String TAX_PRICE = "tax_price";

    public static final String EXPIRE_DATE = "expire_date";

    public static final String TAX_RATE = "tax_rate";

    public static final String DELIVERY_DAY = "delivery_day";

    public static final String EFFECTIVE_DATE = "effective_date";

    public static final String DISABLED = "disabled";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}