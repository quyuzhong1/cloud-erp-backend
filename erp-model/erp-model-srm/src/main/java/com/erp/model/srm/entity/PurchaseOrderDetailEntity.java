package com.erp.model.srm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 采购订单明细表（已确认）
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("purchase_order_detail")
public class PurchaseOrderDetailEntity extends BaseEntity<PurchaseOrderDetailEntity> {

    /**
    * 采购订单id
    */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
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
    * 采购数量
    */
    @TableField("purchase_qty")
    private Integer purchaseQty;
    /**
    * 采购金额
    */
    @TableField("purchase_amount")
    private BigDecimal purchaseAmount;
    /**
    * 预计交货日期
    */
    @TableField("plan_delivery_date")
    private LocalDate planDeliveryDate;
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
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 是否加急（false否，true是）
    */
    @TableField("is_urgent")
    private Boolean isUrgent;
    /**
    * 币种符号
    */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 执行状态
    */
    @TableField("execution_status")
    private String executionStatus;
    /**
    * 确认类型（auto 系统，manual 手动）
    */
    @TableField("confirm_type")
    private String confirmType;
    /**
    * 单据编号（订单单号）
    */
    @TableField("code")
    private String code;
    /**
    * 客户联系人id
    */
    @TableField("purchase_user_id")
    private String purchaseUserId;
    /**
    * 客户联系人名称
    */
    @TableField("purchase_user_name")
    private String purchaseUserName;
    /**
    * 客户id
    */
    @TableField("purchase_org_id")
    private String purchaseOrgId;
    /**
    * 客户名称
    */
    @TableField("purchase_org_name")
    private String purchaseOrgName;
    /**
    * 目的仓库id
    */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
    * 目的仓库名称
    */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
    * 单据类型,字典PurchaseOrderType类型
    */
    @TableField("type")
    private String type;


    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String PURCHASE_AMOUNT = "purchase_amount";

    public static final String PLAN_DELIVERY_DATE = "plan_delivery_date";

    public static final String IS_GIFT = "is_gift";

    public static final String REMARK = "remark";

    public static final String TAX_RATE = "tax_rate";

    public static final String IS_URGENT = "is_urgent";

    public static final String CURRENCY_SYMBOL = "currency_symbol";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String EXECUTION_STATUS = "execution_status";

    public static final String CONFIRM_TYPE = "confirm_type";

    public static final String CODE = "code";

    public static final String PURCHASE_USER_ID = "purchase_user_id";

    public static final String PURCHASE_USER_NAME = "purchase_user_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}