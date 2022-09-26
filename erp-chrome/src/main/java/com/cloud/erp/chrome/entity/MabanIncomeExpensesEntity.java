package com.cloud.erp.chrome.entity;

import java.math.BigDecimal;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author yl
 * @since 2022-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sales_order_mabang")
public class MabanIncomeExpensesEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单编号
     */
    @TableField("order_no")
    @Alias("订单编号")
    private String orderNo;

    /**
     * 交易号
     */
    @TableField("trade_no")
    @Alias("交易号")
    private String tradeNo;

    /**
     * 店铺名称
     */
    @TableField("shops_name")
    @Alias("店铺名称")
    private String shopsName;

    /**
     * 国家名
     */
    @TableField("country_name")
    @Alias("国家")
    private String countryName;

    /**
     * sku 
     */
    @TableField("sku_info")
    @Alias("库存sku*数量")
    private String skuInfo;

    /**
     * 发货日期
     */
    @TableField("invoice_date")
    @Alias("发货日期")
    private String invoiceDate;

    /**
     * 平台
     */
    @TableField("platform")
    @Alias("平台")
    private String platform;

    /**
     * 汇率
     */
    @TableField("exchange_rate")
    @Alias("汇率")
    private BigDecimal exchangeRate;

    /**
     * 收入
     */
    @TableField("income")
    @Alias("收入-小计")
    private BigDecimal income;

    /**
     * 支出
     */
    @TableField("expenditure")
    @Alias("支出-运费")
    private BigDecimal expenditure;

    /**
     * 毛利
     */
    @TableField("gross_profit")
    @Alias("毛利")
    private BigDecimal grossProfit;

    /**
     * 毛利率
     */
    @TableField("gross_profit_rate")
    @Alias("毛利率")
    private BigDecimal grossProfitRate;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 订单日期
     */
    @TableField("order_date")
    @Alias("发货日期")
    private Date orderDate;


}
