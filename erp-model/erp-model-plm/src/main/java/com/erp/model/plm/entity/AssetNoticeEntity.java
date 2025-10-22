package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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
@TableName("asset_notice")
public class AssetNoticeEntity extends BaseEntity<AssetNoticeEntity> {

    /**
    * 单号
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
    * 申请人id
    */
    @TableField("apply_user_id")
    private String applyUserId;
    /**
    * 申请人名称
    */
    @TableField("apply_user_name")
    private String applyUserName;
    /**
    * 申请部门id
    */
    @TableField("apply_dept_id")
    private String applyDeptId;
    /**
    * 申请部门名称
    */
    @TableField("apply_dept_name")
    private String applyDeptName;
    /**
    * 作废原因
    */
    @TableField("invalid_reason")
    private String invalidReason;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 作废日期
    */
    @TableField("invalid_date")
    private LocalDate invalidDate;
    /**
    * 申请日期
    */
    @TableField(value = "apply_date")
    private LocalDate applyDate;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;


    public static final String CODE = "code";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPLY_USER_ID = "apply_user_id";

    public static final String APPLY_USER_NAME = "apply_user_name";

    public static final String APPLY_DEPT_ID = "apply_dept_id";

    public static final String APPLY_DEPT_NAME = "apply_dept_name";

    public static final String INVALID_REASON = "invalid_reason";

    public static final String REMARK = "remark";

    public static final String INVALID_DATE = "invalid_date";

    public static final String APPLY_DATE = "apply_date";

    public static final String INVALID_STATUS = "invalid_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}