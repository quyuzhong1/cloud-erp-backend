package com.cloud.erp.chrome.entity;

import java.math.BigDecimal;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 管易云 erp 发货信息表
 * </p>
 *
 * @author yl
 * @since 2022-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("gyy_shipments_info")
public class GyuShipmentsInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 店铺名称
     */
    @TableField("shops_name")
    @Alias("店铺名称")
    private String shopsName;

    /**
     * 发货单号
     */
    @TableField("invoice_no")
    @Alias("发货单号")
    private String invoiceNo;

    /**
     * 平台单号
     */
    @TableField("platform_no")
    @Alias("平台编号")
    private String platformNo;

    /**
     * 发货时间
     */
    @TableField("invoice_time")
    @Alias("发货时间")
    private String invoiceTime;

    /**
     * 会员名
     */
    @TableField("member_name")
    @Alias("会员名称")
    private String memberName;

    /**
     * 商品代码
     */
    @TableField("goods_sku")
    @Alias("商品代码")
    private String goodsSku;

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
    @TableField("unit_cost")
    @Alias("成本单价")
    private BigDecimal unitCost;

    /**
     * 成本价
     */
    @TableField("cost_price")
    @Alias("成本金额")
    private BigDecimal costPrice;

    /**
     * 实践单价
     */
    @TableField("reality_unit_price")
    @Alias("实际单价")
    private BigDecimal realityUnitPrice;

    /**
     * 实际金额
     */
    @TableField("reality_price")
    @Alias("实际金额")
    private BigDecimal realityPrice;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    @Alias("仓库名称")
    private String warehouseName;

    /**
     * 商品条码
     */
    @TableField("goods_bar_code")
    @Alias("商品条码")
    private String goodsBarCode;

    /**
     * 物流单号
     */
    @TableField("tracking_number")
    @Alias("物流单号")
    private String trackingNumber;

    /**
     * 物流公司
     */
    @TableField("logistics_company")
    @Alias("物流公司")
    private String logisticsCompany;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;


}
