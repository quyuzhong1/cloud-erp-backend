package com.erp.server.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 采购入库单
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_storage")
public class PurchaseStorageEntity extends BaseEntity<PurchaseStorageEntity> {

    /**
     * 采购入库单号
     */
    @TableField("code")
    private String code;

    /**
     * 审核状态 
     */
    @TableField("approve_status")
    private String approveStatus;

    /**
     * 入库日期
     */
    @TableField("storage_date")
    private Date storageDate;

    /**
     * 入库员id
     */
    @TableField("storage_user_id")
    private String storageUserId;

    /**
     * 入库员名称
     */
    @TableField("storage_user_name")
    private String storageUserName;

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
     * 采购部门id
     */
    @TableField("storage_dept_id")
    private String storageDeptId;

    /**
     * 采购部门名称
     */
    @TableField("storage_dept_name")
    private String storageDeptName;

    /**
     * 采购部门id
     */
    @TableField("purchase_dept_id")
    private String purchaseDeptId;

    /**
     * 采购部门名称
     */
    @TableField("purchase_dept_name")
    private String purchaseDeptName;

    /**
     * 新品首批（false否,true是）
     */
    @TableField("is_first_mass_product")
    private Boolean isFirstMassProduct;

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
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
     * 收货主表id
     */
    @TableField("receive_id")
    private String receiveId;

    /**
     * 作废状态（0未作废，1已作废）
     */
    @TableField("invalid_status")
    private String invalidStatus;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private Date invalidTime;

    /**
     * 作废原因
     */
    @TableField("invalid_remark")
    private String invalidRemark;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private Date approveTime;

    /**
     * 审核人名称
     */
    @TableField("approve_user_name")
    private String approveUserName;

    /**
     * 审核人id
     */
    @TableField("approve_user_id")
    private String approveUserId;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String STORAGE_DATE = "storage_date";

    public static final String STORAGE_USER_ID = "storage_user_id";

    public static final String STORAGE_USER_NAME = "storage_user_name";

    public static final String PURCHASE_USER_ID = "purchase_user_id";

    public static final String PURCHASE_USER_NAME = "purchase_user_name";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String STORAGE_DEPT_ID = "storage_dept_id";

    public static final String STORAGE_DEPT_NAME = "storage_dept_name";

    public static final String PURCHASE_DEPT_ID = "purchase_dept_id";

    public static final String PURCHASE_DEPT_NAME = "purchase_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String PURCHASE_ORDER_ID = "purchase_order_id";

    public static final String RECEIVE_ID = "receive_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

}
