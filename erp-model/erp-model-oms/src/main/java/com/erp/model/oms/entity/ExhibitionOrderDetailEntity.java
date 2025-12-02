package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.utils.SampleDocumentAuditUtil;


/**
 * <p>
 * 展会订单详情
 * </p>
 *
 * @author jack
 * @since 2025-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("exhibition_order_detail")
public class ExhibitionOrderDetailEntity extends BaseEntity<ExhibitionOrderDetailEntity> 
        implements SampleDocumentAuditUtil.SampleDocumentDetail {

    /**
    * 台账id
    */
    @TableField("sample_ledger_id")
    private String sampleLedgerId;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
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
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 销售数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 单价
    */
    @TableField("price")
    private BigDecimal price;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 销售金额
    */
    @TableField("amount")
    private BigDecimal amount;
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
    * 是否赠品
    */
    @TableField("is_gift")
    private Boolean isGift;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 采购单价
    */
    @TableField("purchase_price")
    private BigDecimal purchasePrice;
    /**
    * 销售总成本
    */
    @TableField("sale_cost")
    private BigDecimal saleCost;
    /**
    * 销售毛利
    */
    @TableField("sale_profit")
    private BigDecimal saleProfit;
    /**
    * 销售毛利率
    */
    @TableField("sale_profit_rate")
    private BigDecimal saleProfitRate;
    /**
    * 含税的销售金额折后
    */
    @TableField("tax_amount")
    private BigDecimal taxAmount;
    /**
    * 销售金额本位币
    */
    @TableField("amount_local_currency")
    private BigDecimal amountLocalCurrency;
    /**
    * 价税合计本位币
    */
    @TableField("all_amount_local_currency")
    private BigDecimal allAmountLocalCurrency;
    /**
    * 折扣额
    */
    @TableField("discount_amount")
    private BigDecimal discountAmount;
    /**
    * 含税的销售金额折扣前
    */
    @TableField("tax_amount_before")
    private BigDecimal taxAmountBefore;
    /**
    * 销售金额计算汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 税额
    */
    @TableField("tax")
    private BigDecimal tax;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * 成本来源
    */
    @TableField("cost_source")
    private String costSource;
    /**
    * 含税单价
    */
    @TableField("tax_price")
    private BigDecimal taxPrice;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String PRICE = "price";

    public static final String TAX_RATE = "tax_rate";

    public static final String AMOUNT = "amount";

    public static final String CURRENCY = "currency";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String IS_GIFT = "is_gift";

    public static final String REMARK = "remark";

    public static final String PURCHASE_PRICE = "purchase_price";

    public static final String SALE_COST = "sale_cost";

    public static final String SALE_PROFIT = "sale_profit";

    public static final String SALE_PROFIT_RATE = "sale_profit_rate";

    public static final String TAX_AMOUNT = "tax_amount";

    public static final String AMOUNT_LOCAL_CURRENCY = "amount_local_currency";

    public static final String ALL_AMOUNT_LOCAL_CURRENCY = "all_amount_local_currency";

    public static final String DISCOUNT_AMOUNT = "discount_amount";

    public static final String TAX_AMOUNT_BEFORE = "tax_amount_before";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String TAX = "tax";

    public static final String BOM_VERSION = "bom_version";

    public static final String COST_SOURCE = "cost_source";

    public static final String TAX_PRICE = "tax_price";

    @Override
    public Serializable pkVal() {
        return null;
    }

}