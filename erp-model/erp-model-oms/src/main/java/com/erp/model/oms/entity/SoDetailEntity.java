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
 * 销售订单详情
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_detail")
public class SoDetailEntity extends BaseEntity<SoDetailEntity> {

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
     * 平台sku no
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    /**
     * 销售数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 发货状态
     * unShipped未发货
     * partialShipment 部分发货
     */
    @TableField("delivery_status")
    private String deliveryStatus;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

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
     * 销售金额 （折后）
     * 销售金额= 含税单价*数量-折扣-税额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 金额含税（折扣后）
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

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
     * 是否关闭 true 是
     */
    @TableField("is_close")
    private Boolean isClose;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 对应金蝶详情id
     */
    @TableField("kingdee_detail_id")
    private String kingdeeDetailId;

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
     * 销售金额(本位币)
     */
    @TableField("amount_local_currency")
    private BigDecimal amountLocalCurrency;

    /**
     * 价税合计(本位币)
     */
    @TableField("all_amount_local_currency")
    private BigDecimal allAmountLocalCurrency;

    /**
     * 折扣金额
     */
    @TableField(value = "discount_amount")
    private BigDecimal discountAmount;

    /**
     * 税额
     *  不含税 税额=（单价*数量-折扣额）* (税率/100) ps (税率/100) 为1 因为税率为100
     *  含税 税额=（（含税单价*数量-折扣额）/(100+税率)）*税率
     */
    @TableField(value = "tax")
    private BigDecimal tax;

    /**
     * 含税的销售金额（折扣前）
     */
    @TableField(value = "tax_amount_before")
    private BigDecimal taxAmountBefore;

    /**
     * 汇率
     */
    @TableField(value = "exchange_rate")
    private BigDecimal exchangeRate;

    /**
     * 发货数量
     */
    @TableField(value = "delivery_qty")
    private Integer deliveryQty;

    /**
     * 冻结数量
     */
    @TableField(value = "frozen_qty")
    private Integer frozenQty;
    /**
     * bom 版本
     */
    @TableField(value = "bom_version")
    private String bomVersion;


    @TableField(exist = false)
    private String approveStatus;

    @TableField(exist = false)
    private Integer index;

    @TableField(exist = false)
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

    public static final String IS_REISSUE = "is_reissue";

    public static final String IS_CLOSE = "is_close";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
