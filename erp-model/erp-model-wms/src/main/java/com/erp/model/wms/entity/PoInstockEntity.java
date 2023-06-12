package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@TableName("po_instock")
public class PoInstockEntity extends BaseEntity<PoInstockEntity> {

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
    @TableField("stock_in_date")
    private LocalDate stockInDate;

    /**
     * 入库员id
     */
    @TableField("stock_in_user_id")
    private String stockInUserId;

    /**
     * 入库员名称
     */
    @TableField("stock_in_user_name")
    private String stockInUserName;

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
     * 入库部门id
     */
    @TableField("stock_in_dept_id")
    private String stockInDeptId;

    /**
     * 采入库部门名称
     */
    @TableField("stock_in_dept_name")
    private String stockInDeptName;

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
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 作废状态（false未作废，true已作废）
     */
    @TableField("invalid_status")
    private Boolean invalidStatus;

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
    private LocalDateTime approveTime;

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

    /**
     * 采购订单id
     */
    @TableField("purchase_order_id")
    private String purchaseOrderId;

    /**
     * 采购订单编码
     */
    @TableField("purchase_order_code")
    private String purchaseOrderCode;

    /**
     * 是否是委外
     */
    @TableField("is_subcontract")
    private Boolean isSubcontract;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    @TableField("sync_kingdee_status")
    private String syncKingdeeStatus;

    /**
     * 同步金蝶时间
     */
    @TableField("sync_kingdee_time")
    private LocalDateTime syncKingdeeTime;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    @TableField("sync_operate")
    private String syncOperate;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String STOCK_IN_DATE = "stock_in_date";

    public static final String STOCK_IN_USER_ID = "stock_in_user_id";

    public static final String STOCK_IN_USER_NAME = "stock_in_user_name";

    public static final String PURCHASE_USER_ID = "purchase_user_id";

    public static final String PURCHASE_USER_NAME = "purchase_user_name";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String STOCK_IN_DEPT_ID = "stock_in_dept_id";

    public static final String STOCK_IN_DEPT_NAME = "stock_in_dept_name";

    public static final String PURCHASE_DEPT_ID = "purchase_dept_id";

    public static final String PURCHASE_DEPT_NAME = "purchase_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

}
