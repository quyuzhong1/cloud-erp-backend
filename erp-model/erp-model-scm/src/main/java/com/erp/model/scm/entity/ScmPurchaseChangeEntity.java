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
 * 销售需求明细表
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("scm_purchase_change")
public class ScmPurchaseChangeEntity extends BaseEntity<ScmPurchaseChangeEntity> {

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
     * 采购订单id
     */
    @TableField("purch_order_id")
    private String purchOrderId;

    /**
     * 变更日期
     */
    @TableField("change_date")
    private Date changeDate;

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
    @TableField("purch_org_id")
    private String purchOrgId;

    /**
     * 采购组织名称
     */
    @TableField("purch_org_name")
    private String purchOrgName;

    /**
     * 新品首批（false否,true是）
     */
    @TableField("is_first_mass_product")
    private Boolean isFirstMassProduct;


    public static final String AUDIT_STATUS = "audit_status";

    public static final String CODE = "code";

    public static final String PURCH_ORDER_ID = "purch_order_id";

    public static final String CHANGE_DATE = "change_date";

    public static final String CHANGE_USER_ID = "change_user_id";

    public static final String CHANGE_USER_NAME = "change_user_name";

    public static final String CHANGE_DEPT_ID = "change_dept_id";

    public static final String CHANGE_DEPT_NAME = "change_dept_name";

    public static final String PURCH_ORG_ID = "purch_org_id";

    public static final String PURCH_ORG_NAME = "purch_org_name";

    public static final String IS_FIRST_MASS_PRODUCT = "is_first_mass_product";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
