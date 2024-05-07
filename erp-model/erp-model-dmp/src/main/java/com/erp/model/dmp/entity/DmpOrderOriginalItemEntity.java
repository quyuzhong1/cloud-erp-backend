package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * <p>
 * 订单商品信息拆分前表
 * </p>
 */
@Getter
@Setter
@TableName("dmp_order_original_item")
public class DmpOrderOriginalItemEntity{
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 订单表id
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 商品id
     */
    @TableField("item_id")
    private String itemId;

    /**
     * 平台sku
     */
    @TableField("platform_sku")
    private String platformSku;

    /**
     * 平台原始sku数量
     */
    @TableField("platform_quantity")
    private Integer platformQuantity;

    /**
     * 商品名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 商品图片
     */
    @TableField(value = "picture_url")
    private String pictureUrl;

    /**
     * 商品原始售价
     */
    @TableField("sell_price_origin")
    private BigDecimal sellPriceOrigin;

    /**
     * 商品售价
     */
    @TableField("sell_price")
    private BigDecimal sellPrice;

    /**
     * 商品数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 商品单位
     */
    @TableField("product_unit")
    private String productUnit;

    /**
     * 是否是赠品 1. 是 2. 否
     */
    @TableField("is_gift")
    private Integer isGift;

    /**
     * 缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
     */
    @TableField("has_goods")
    private Integer hasGoods;

    /**
     * 是否是组合商品 1.组合 2非组合
     */
    @TableField("is_combo")
    private Integer isCombo;

    /**
     * 订单商品备注
     */
    @TableField("item_remark")
    private String itemRemark;

    /**
     * 商品多属性
     */
    @TableField(value = "specifics")
    private String specifics;

    /**
     * 商品状态 1：未付款 2：未发货 3：已发货 4：已作废
     */
    @TableField("status")
    private String status;

    /**
     * 商品仓位
     */
    @TableField("stock_grid")
    private String stockGrid;

    /**
     * sku
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
     */
    @TableField("stock_status")
    private Integer stockStatus;

    /**
     * 商品仓库编号
     */
    @TableField("stock_warehouse_id")
    private String stockWarehouseId;

    /**
     * erp平台商品id
     */
    @TableField("erp_order_item_id")
    private String erpOrderItemId;

    /**
     * 品类id
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 品类名称
     */
    @TableField(value = "category_name")
    private String categoryName;

    /**
     * 品牌id
     */
    @TableField("brand_id")
    private String brandId;

    /**
     * 品牌名称
     */
    @TableField("brand_name")
    private String brandName;

    /**
     * 汇率
     */
    @TableField("currency_rate")
    private BigDecimal currencyRate;

    /**
     * 新品标记 1 为新品 0 为非新品
     */
    @TableField("new_sign")
    private Integer newSign;

    /**
     * 折扣后的原币种金额 对应管易amount_after 马帮sell_price*quantity 金蝶fAllAmount
     */
    @TableField("amount_after")
    private BigDecimal amountAfter;

    /**
     * 刷新数据使用
     */
    @TableField("refresh_status")
    private Boolean refreshStatus;

    /**
     * 计算汇率
     */
    @TableField("cny_settle_rate")
    private BigDecimal cnySettleRate;

    /**
     * 成本价
     */
    @TableField("cost_price")
    private BigDecimal costPrice;

    /**
     * 清洗前成本价
     */
    @TableField(value = "original_cost_price")
    private BigDecimal originalCostPrice;

    /**
     * 清洗前销售额
     */
    @TableField(value = "original_amount_after")
    private BigDecimal originalAmountAfter;

    /**
     * 清洗前数量
     */
    @TableField(value = "original_quantity")
    private Integer originalQuantity;

    /**
     * 来源明细id
     */
    @TableField(value = "source_item_id")
    private String sourceItemId;


}
