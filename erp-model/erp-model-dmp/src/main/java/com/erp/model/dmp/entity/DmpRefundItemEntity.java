package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款商品明细表
 * @TableName dmp_refund_item
 */
@TableName(value ="dmp_refund_item")
@Data
public class DmpRefundItemEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 退款表id
     */
    @TableField(value = "refund_id")
    private String refundId;

    /**
     * sku编号
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 订单原始商品数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 退款商品数量
     */
    @TableField(value = "refund_num")
    private Integer refundNum;

    /**
     * 是否属于组合sku：0. 否 1. 是
     */
    @TableField(value = "is_combo")
    private Integer isCombo;

    /**
     * erp平台商品id
     */
    @TableField(value = "erp_order_item_id")
    private String erpOrderItemId;

    /**
     * 折扣后订单总金额
     */
    @TableField(value = "amount_after")
    private BigDecimal amountAfter;

    /**
     * 原始sku
     */
    @TableField(value = "original_sku_no")
    private String originalSkuNo;

    /**
     * 清洗后成本价
     */
    @TableField(value = "clean_cost_price")
    private BigDecimal cleanCostPrice;

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
     * 是否拆分订单 1.拆分 2.非拆分
     */
    @TableField(value = "is_split_sku")
    private Integer isSplitSku;

    /**
     * 是否是赠品 1. 是 2. 否
     */
    @TableField(value = "is_gift")
    private Integer isGift;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpRefundItemEntity{" +
                "refundId='" + refundId + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", quantity=" + quantity +
                ", refundNum=" + refundNum +
                ", isCombo=" + isCombo +
                ", amountAfter=" + amountAfter +
                '}';
    }
}