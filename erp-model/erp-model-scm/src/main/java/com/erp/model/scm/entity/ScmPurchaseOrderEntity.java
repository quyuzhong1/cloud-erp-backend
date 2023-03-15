package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 采购订单表
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_order")
public class ScmPurchaseOrderEntity extends BaseEntity<ScmPurchaseOrderEntity> {

    /**
     * 审核状态 
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 单据编号
     */
    @TableField("code")
    private String code;

    /**
     * 采购日期
     */
    @TableField("purch_date")
    private Date purchDate;

    /**
     * 采购员id
     */
    @TableField("purch_user_id")
    private String purchUserId;

    /**
     * 采购员名称
     */
    @TableField("purch_user_name")
    private String purchUserName;

    /**
     * 采购组织id
     */
    @TableField("purch_org_id")
    private String purchOrgId;

    /**
     * 采购组织名称
     */
    @TableField("purch_org_name")
    private String purchOrgName;

    /**
     * 采购部门id
     */
    @TableField("purch_dept_id")
    private String purchDeptId;

    /**
     * 采购部门名称
     */
    @TableField("purch_dept_name")
    private String purchDeptName;

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
    @TableField("invaild_time")
    private Date invalidTime;

    /**
     * 到货状态（0未到货，1部分到货，2已到货）
     */
    @TableField("arrival_status")
    private String arrivalStatus;

    /**
     * 到货时间
     */
    @TableField("arrival_time")
    private Date arrivalTime;


    public static final String AUDIT_STATUS = "audit_status";

    public static final String CODE = "code";

    public static final String PURCH_DATE = "purch_date";

    public static final String PURCH_USER_ID = "purch_user_id";

    public static final String PURCH_USER_NAME = "purch_user_name";

    public static final String PURCH_ORG_ID = "purch_org_id";

    public static final String PURCH_ORG_NAME = "purch_org_name";

    public static final String PURCH_DEPT_ID = "purch_dept_id";

    public static final String PURCH_DEPT_NAME = "purch_dept_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVAILD_TIME = "invaild_time";

    public static final String ARRIVAL_STATUS = "arrival_status";

    public static final String ARRIVAL_TIME = "arrival_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
