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
 * @TableName dmp_delivery_detail_item
 */
@TableName(value ="dmp_delivery_detail_item")
@Data
public class DmpDeliveryDetailItemEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 发货详情表id
     */
    @TableField(value = "delivery_detail_id")
    private String deliveryDetailId;

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
     * 商品sku编号
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 商品名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 商品成本价
     */
    @TableField(value = "cost_price")
    private BigDecimal costPrice;

    /**
     * 商品单价
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
     * 属性
     */
    @TableField(value = "specifics")
    private String specifics;

    /**
     * 订单商品备注
     */
    @TableField(value = "item_remark")
    private String itemRemark;

    /**
     * 仓库
     */
    @TableField(value = "stock_name")
    private String stockName;

    /**
     * 库位
     */
    @TableField(value = "warehouse_location")
    private String warehouseLocation;

    /**
     * 总售价
     */
    @TableField(value = "amount")
    private BigDecimal amount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpDeliveryDetailItemEntity{" +
                "deliveryDetailId='" + deliveryDetailId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", platformSku='" + platformSku + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", itemName='" + itemName + '\'' +
                ", costPrice=" + costPrice +
                ", sellPrice=" + sellPrice +
                ", quantity=" + quantity +
                ", productUnit='" + productUnit + '\'' +
                ", isGift=" + isGift +
                ", specifics='" + specifics + '\'' +
                ", itemRemark='" + itemRemark + '\'' +
                ", stockName='" + stockName + '\'' +
                ", warehouseLocation='" + warehouseLocation + '\'' +
                ", amount=" + amount +
                '}';
    }
}