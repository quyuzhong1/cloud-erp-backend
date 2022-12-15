package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * @TableName dmp_order_item
 */
@TableName(value ="dmp_order_item")
@Data
public class DmpOrderItemEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 订单表id
     */
    @TableField(value = "order_id")
    private String orderId;

    /**
     * 商品id
     */
    @TableField(value = "item_id")
    private String itemId;

    /**
     * 平台sku
     */
    @TableField(value = "platform_sku")
    private String platformSku;

    /**
     * 平台原始sku数量
     */
    @TableField(value = "platform_quantity")
    private Integer platformQuantity;

    /**
     * 商品名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 商品图片
     */
    @TableField(value = "picture_url")
    private String pictureUrl;

    /**
     * 商品成本价
     */
    @TableField(value = "cost_price")
    private BigDecimal costPrice;

    /**
     * 商品原始售价
     */
    @TableField(value = "sell_price_origin")
    private BigDecimal sellPriceOrigin;

    /**
     * 商品售价
     */
    @TableField(value = "sell_price")
    private BigDecimal sellPrice;

    /**
     * 商品数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 商品单位
     */
    @TableField(value = "product_unit")
    private String productUnit;

    /**
     * 是否是赠品 1. 是 2. 否
     */
    @TableField(value = "is_gift")
    private Integer isGift;

    /**
     * 缺货订单 0.正在计算是否缺货 1.有货 2.缺货 3.已补货
     */
    @TableField(value = "has_goods")
    private Integer hasGoods;

    /**
     * 是否是组合商品 1.组合 2非组合
     */
    @TableField(value = "is_combo")
    private Integer isCombo;

    /**
     * 订单商品备注
     */
    @TableField(value = "item_remark")
    private String itemRemark;

    /**
     * 商品多属性
     */
    @TableField(value = "specifics")
    private String specifics;

    /**
     * 商品状态 1：未付款 2：未发货 3：已发货 4：已作废
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 商品仓位
     */
    @TableField(value = "stock_grid")
    private String stockGrid;

    /**
     * sku
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 库存状态：1.自动创建 2.待开发 3.正常 4.清仓 5.停止销售
     */
    @TableField(value = "stock_status")
    private Integer stockStatus;

    /**
     * 商品仓库编号
     */
    @TableField(value = "stock_warehouse_id")
    private String stockWarehouseId;

    /**
     * erp平台商品id
     */
    @TableField(value = "erp_order_item_id")
    private String erpOrderItemId;

    /**
     * 品类id
     */
    @TableField(value = "category_id")
    private String categoryId;

    /**
     * 品类
     */
    @TableField(value = "category_name")
    private String categoryName;

    /**
     * 品牌id
     */
    @TableField(value = "brand_id")
    private String brandId;

    /**
     * 品牌
     */
    @TableField(value = "brand_name")
    private String brandName;

    /**
     * 汇率
     */
    @TableField(value = "currency_rate")
    private BigDecimal currencyRate;

    /**
     * 新品标识 1为新品 0 为非新品
     */
    @TableField(value = "new_sign")
    private Integer newSign;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpOrderItemEntity{" +
                "orderId='" + orderId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", platformSku='" + platformSku + '\'' +
                ", platformQuantity=" + platformQuantity +
                ", itemName='" + itemName + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", costPrice=" + costPrice +
                ", sellPriceOrigin=" + sellPriceOrigin +
                ", sellPrice=" + sellPrice +
                ", quantity=" + quantity +
                ", productUnit='" + productUnit + '\'' +
                ", isGift=" + isGift +
                ", hasGoods=" + hasGoods +
                ", isCombo=" + isCombo +
                ", itemRemark='" + itemRemark + '\'' +
                ", specifics='" + specifics + '\'' +
                ", status=" + status +
                ", stockGrid='" + stockGrid + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", stockStatus=" + stockStatus +
                ", stockWarehouseId='" + stockWarehouseId + '\'' +
                ", erpOrderItemId='" + erpOrderItemId + '\'' +
                '}';
    }
}