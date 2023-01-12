package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退货订单商品明细表
 * @TableName dmp_return_order_item
 */
@TableName(value ="dmp_return_order_item")
@Data
public class DmpReturnOrderItemEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 退货订单表id
     */
    @TableField(value = "return_order_id")
    private String returnOrderId;

    /**
     * sku编号
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 商品名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 买家购买数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 商品单位
     */
    @TableField(value = "product_unit")
    private String productUnit;

    /**
     * 商品图片地址
     */
    @TableField(value = "picture_url")
    private String pictureUrl;

    /**
     * 售价
     */
    @TableField(value = "sell_price")
    private BigDecimal sellPrice;

    /**
     * 物品属性
     */
    @TableField(value = "specifics")
    private String specifics;

    /**
     * 状态 1待处理 2验货入库 3自然耗损
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 折扣后订单总金额
     */
    @TableField(value = "amount_after")
    private BigDecimal amountAfter;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DmpReturnOrderItemEntity{" +
                "returnOrderId='" + returnOrderId + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", productUnit='" + productUnit + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", sellPrice=" + sellPrice +
                ", specifics='" + specifics + '\'' +
                ", status=" + status +
                ", amountAfter=" + amountAfter +
                '}';
    }
}