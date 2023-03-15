package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_order_detail")
public class ScmPurchaseOrderDetailEntity extends BaseEntity<ScmPurchaseOrderDetailEntity> {

    /**
     * 采购订单id
     */
    @TableField("purch_order_id")
    private String purchOrderId;

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
     * 报关型号
     */
    @TableField("declare_model")
    private String declareModel;

    /**
     * 报关名称
     */
    @TableField("declare_name")
    private String declareName;

    /**
     * 单箱数量
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 含税单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 币别
     */
    @TableField("currency")
    private String currency;

    /**
     * 采购数量
     */
    @TableField("purch_qty")
    private Integer purchQty;

    /**
     * 采购金额
     */
    @TableField("purch_amount")
    private BigDecimal purchAmount;

    /**
     * 预计交货日期
     */
    @TableField("plan_delivery_date")
    private Date planDeliveryDate;

    /**
     * 收料组织id
     */
    @TableField("receive_org_id")
    private String receiveOrgId;

    /**
     * 收料组织名称
     */
    @TableField("receive_org_name")
    private String receiveOrgName;

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

    /**
     * 是否是赠品（false否，true是）
     */
    @TableField("is_gift")
    private Boolean isGift;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;


    public static final String PURCH_ORDER_ID = "purch_order_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_MODEL = "declare_model";

    public static final String DECLARE_NAME = "declare_name";

    public static final String QTY = "qty";

    public static final String PRICE = "price";

    public static final String CURRENCY = "currency";

    public static final String PURCH_QTY = "purch_qty";

    public static final String PURCH_AMOUNT = "purch_amount";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String IS_GIFT = "is_gift";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
