package com.erp.model.scm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 委外变更单
 * </p>
 *
 * @author will
 * @since 2023-06-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("subcontract_change_order")
public class SubcontractChangeOrderEntity extends BaseEntity<SubcontractChangeOrderEntity> {


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
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;

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
    * 变更人id
    */
    @TableField("changer_id")
    private String changerId;

    /**
    * 变更人名称
    */
    @TableField("changer_name")
    private String changerName;

    /**
    * 采购部门id
    */
    @TableField("dept_id")
    private String deptId;

    /**
    * 采购部门名称
    */
    @TableField("dept_name")
    private String deptName;

    /**
    * 变更原因
    */
    @TableField("change_reason")
    private String changeReason;

    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;

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
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;

    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;

    /**
    * 来源编码
    */
    @TableField("source_code")
    private String sourceCode;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String CODE = "code";

    public static final String BILL_DATE = "bill_date";

    public static final String RECEIVE_ORG_ID = "receive_org_id";

    public static final String RECEIVE_ORG_NAME = "receive_org_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String CHANGER_ID = "changer_id";

    public static final String CHANGER_NAME = "changer_name";

    public static final String DEPT_ID = "dept_id";

    public static final String DEPT_NAME = "dept_name";

    public static final String CHANGE_REASON = "change_reason";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}