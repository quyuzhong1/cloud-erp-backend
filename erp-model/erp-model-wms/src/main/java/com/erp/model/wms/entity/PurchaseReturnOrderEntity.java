package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 采购退货单
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_return_order")
public class PurchaseReturnOrderEntity extends BaseEntity<PurchaseReturnOrderEntity> {

    /**
     * 审核状态 waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
     * 采购订单编号
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;

    /**
     * 供应商id
     */
    @TableField("supplier_id")
    private String supplierId;

    /**
     * 供应商名称
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 退货人id
     */
    @TableField("return_user_id")
    private String returnUserId;

    /**
     * 退货人名称
     */
    @TableField("return_user_name")
    private String returnUserName;

    /**
     * 退货方式 退货扣款 退货补货
     */
    @TableField("return_mode")
    private String returnMode;

    /**
     * 退货人组织id
     */
    @TableField("return_org_id")
    private String returnOrgId;

    /**
     * 退货人组织名称
     */
    @TableField("return_org_name")
    private String returnOrgName;

    /**
     * 退货原因
     */
    @TableField("return_remark")
    private String returnRemark;

    /**
     * 退货日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

    /**
     * 作废描述
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 退货来源 （质检单，签收单，采购订单）
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 退货仓库id
     */
    @TableField("return_warehouse_id")
    private String returnWarehouseId;

    /**
     * 退货仓库名称
     */
    @TableField("return_warehouse_name")
    private String returnWarehouseName;

    /**
     * 采购员id
     */
    @TableField("purchase_user_id")
    private String purchaseUserId;

    /**
     * 采购员名称
     */
    @TableField("purchase_user_name")
    private String purchaseUserName;

    /**
     * 退货仓库联系人id
     */
    @TableField("supplier_contact_id")
    private String supplierContactId;

    /**
     * 退货仓库联系人id
     */
    @TableField("supplier_contact_name")
    private String supplierContactName;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String PURCHASE_ORDER_CODE = "purchase_order_code";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String RETURN_USER_ID = "return_user_id";

    public static final String RETURN_USER_NAME = "return_user_name";

    public static final String RETURN_WAY = "return_way";

    public static final String RETURN_SOURCE = "return_source";

    public static final String RETURN_ORG_ID = "return_org_id";

    public static final String RETURN_ORG_NAME = "return_org_name";

    public static final String RECEICE_REASON = "receice_reason";

    public static final String RECEICE_COST = "receice_cost";

    public static final String RECEICE_SHIPPING_COST = "receice_shipping_cost";

    public static final String OTHER_COST = "other_cost";

    public static final String RETURN_TIME = "return_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
