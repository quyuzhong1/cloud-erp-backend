package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_change_detail")
public class PurchaseChangeDetailEntity extends BaseEntity<PurchaseChangeDetailEntity> {

    /**
     * 采购变更单id
     */
    @TableField("purchase_change_id")
    private String purchaseChangeId;

    /**
     * 采购订单明细id
     */
    @TableField("purchase_order_detail_id")
    private String purchaseOrderDetailId;

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
     * 原采购数量
     */
    @TableField("old_qty")
    private Integer oldQty;

    /**
     * 原含税单价
     */
    @TableField("old_price")
    private BigDecimal oldPrice;

    /**
     * 原含税金额
     */
    @TableField("old_amount")
    private BigDecimal oldAmount;

    /**
     * 新采购数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 新含税单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 新含税金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 变更备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 交货仓库id
     */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;

    /**
     * 交货仓库名称
     */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;


    public static final String PURCHASE_CHANGE_ID = "purchase_change_id";

    public static final String PURCHASE_ORDER_DETAIL_ID = "purchase_order_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String OLD_QTY = "old_qty";

    public static final String OLD_PRICE = "old_price";

    public static final String OLD_AMOUNT = "old_amount";

    public static final String QTY = "qty";

    public static final String PRICE = "price";

    public static final String AMOUNT = "amount";

    public static final String REMARK = "remark";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
