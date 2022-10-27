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
 * 管易云ERP 发货信息表
 * </p>
 *
 * @author yl
 * @since 2022-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sales_order_guanyiyun")
public class OrderGyyDeliverEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    /**
     * 平台类型
     */
    @TableField("platform_type")
    @Alias("店铺类型")
    private String platformType;

    /**
     * 订单类型
     */
    @TableField("order_type")
    @Alias("订单类型")
    private String orderType;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    @Alias("店铺名称")
    private String shopName;

    /**
     * 平台单号
     */
    @TableField("platform_no")
    @Alias("平台单号")
    private String platformNo;

    /**
     * sku 编号
     */
    @TableField("sku_no")
    @Alias("商品代码")
    private String skuNo;

    /**
     * 商品名称
     */
    @TableField("goods_name")
    @Alias("商品名称")
    private String goodsName;

    /**
     * 数量
     */
    @TableField("quantity")
    @Alias("数量")
    private Integer quantity;

    /**
     * 成本单价
     */
    @TableField("cost_price")
    @Alias("成本单价")
    private BigDecimal costPrice;

    /**
     * 实际单价
     */
    @TableField("sales_price")
    @Alias("实际单价")
    private BigDecimal salesPrice;

    /**
     * 金额
     */
    @TableField("amount")
    @Alias("让利后金额")
    private BigDecimal amount;

    /**
     * 订单编号
     */
    @TableField("order_number")
    @Alias("订单编号")
    private String orderNumber;

    /**
     * 发货时间
     */
    @TableField("order_date")
    @Alias("发货时间")
    private Date orderDate;

}
