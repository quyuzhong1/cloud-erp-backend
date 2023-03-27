package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 采购订单表
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("purchase_order")
public class PurchaseOrderEntity extends BaseEntity<PurchaseOrderEntity> {

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
     * 采购日期
     */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

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
     * 作废状态（0未作废，1已作废）
     */
    @TableField("invalid_status")
    private String invalidStatus;

    /**
     * 作废时间
     */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

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


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String PURCHASE_DATE = "purchase_date";

    public static final String PURCHASE_USER_ID = "purchase_user_id";

    public static final String PURCHASE_USER_NAME = "purchase_user_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String PURCHASE_DEPT_ID = "purchase_dept_id";

    public static final String PURCHASE_DEPT_NAME = "purchase_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";


    @Override
    public Serializable pkVal() {
        return null;
    }

}
