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
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("asset_purchase_change")
public class AssetPurchaseChangeEntity extends BaseEntity<AssetPurchaseChangeEntity> {

    /**
    * 资产变更单号
    */
    @TableField("code")
    private String code;
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
    * 单据状态
    */
    @TableField("approve_status")
    private String approveStatus;
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
    * 变更类型
    */
    @TableField("order_type")
    private String orderType;
    /**
    * 变更原因
    */
    @TableField("change_reason")
    private String changeReason;
    /**
    * 来源订单id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源订单号
    */
    @TableField("source_code")
    private String sourceCode;
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
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 同步金蝶id
    */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;


    public static final String CODE = "code";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CHANGE_DATE = "change_date";

    public static final String CHANGE_USER_ID = "change_user_id";

    public static final String CHANGE_USER_NAME = "change_user_name";

    public static final String CHANGE_DEPT_ID = "change_dept_id";

    public static final String CHANGE_DEPT_NAME = "change_dept_name";

    public static final String PURCHASE_ORG_ID = "purchase_org_id";

    public static final String PURCHASE_ORG_NAME = "purchase_org_name";

    public static final String ORDER_TYPE = "order_type";

    public static final String CHANGE_REASON = "change_reason";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_TIME = "invalid_time";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String SYNC_KINGDEE_ID = "sync_kingdee_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}