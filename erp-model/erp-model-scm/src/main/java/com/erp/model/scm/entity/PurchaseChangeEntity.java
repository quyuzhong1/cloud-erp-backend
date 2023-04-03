package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * <p>
 * 销售需求明细表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_change")
public class PurchaseChangeEntity extends BaseEntity<PurchaseChangeEntity> {

    /**
     * 审核状态 
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
     * 变更日期
     */
    @TableField("change_date")
    private LocalDate changeDate;

    /**
     * 变更人id
     */
    @TableField("change_user_id")
    private String changeUserId;

    /**
     * 变更人名称
     */
    @TableField("change_user_name")
    private String changeUserName;

    /**
     * 变更部门id
     */
    @TableField("change_dept_id")
    private String changeDeptId;

    /**
     * 变更部门名称
     */
    @TableField("change_dept_name")
    private String changeDeptName;

    /**
     * 采购组织id
     */
    @TableField("purchase_org_id")
    private String purchaseOrgId;

    /**
     * 采购组织名称
     */
    @TableField("purchase_org_name")
    private String purchaseOrgName;

    /**
     * 新品首批（false否,true是）
     */
    @TableField("is_first_mass_product")
    private Boolean isFirstMassProduct;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private Date approveTime;

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
     * 作废状态（0未作废，1已作废）
     */
    @TableField("invalid_status")
    private String invalidStatus;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDate invalidTime;

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
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

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


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String CHANGE_DATE = "change_date";

    public static final String CHANGE_USER_ID = "change_user_id";

    public static final String CHANGE_USER_NAME = "change_user_name";

    public static final String CHANGE_DEPT_ID = "change_dept_id";

    public static final String CHANGE_DEPT_NAME = "change_dept_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String APPROVE_TIME = "approve_time";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
