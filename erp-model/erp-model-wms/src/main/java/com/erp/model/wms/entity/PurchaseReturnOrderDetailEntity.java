package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 采购退货单明细
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_return_order_detail")
public class PurchaseReturnOrderDetailEntity extends BaseEntity<PurchaseReturnOrderDetailEntity> {

    /**
     * 退货单主表id
     */
    @TableField("return_order_id")
    private String returnOrderId;

    /**
     * skuId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 实退数量
     */
    @TableField("reality_return_qty")
    private Integer realityReturnQty;

    /**
     * 补货数量
     */
    @TableField("replenish_qty")
    private Integer replenishQty;

    /**
     * 扣款数量
     */
    @TableField("deduct_amount_qty")
    private Integer deductAmountQty;

    /**
     * 含税单价
     */
    @TableField("tax_price")
    private String taxPrice;

    /**
     * 总价
     */
    @TableField("total_price")
    private String totalPrice;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String RETURN_ORDER_ID = "return_order_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PLAN_RECEIVE_TIME = "plan_receive_time";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String REALITY_RETURN_QTY = "reality_return_qty";

    public static final String REPLENISH_QTY = "replenish_qty";

    public static final String PRICE = "price";

    public static final String TOTAL_PRICE = "total_price";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
