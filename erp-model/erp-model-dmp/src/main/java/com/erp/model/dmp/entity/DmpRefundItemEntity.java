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
     * 折扣后订单总金额
     */
    @TableField(value = "amount_after")
    private BigDecimal amountAfter;


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