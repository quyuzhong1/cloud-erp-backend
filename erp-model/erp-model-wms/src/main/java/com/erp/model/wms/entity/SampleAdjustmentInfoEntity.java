package com.erp.model.wms.entity;

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
 * 样品调整单
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_adjustment_info")
public class SampleAdjustmentInfoEntity extends BaseEntity<SampleAdjustmentInfoEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审批人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审批人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 是否作废
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 调整人ID
    */
    @TableField("adjustment_user_id")
    private String adjustmentUserId;
    /**
    * 调整人姓名
    */
    @TableField("adjustment_user_name")
    private String adjustmentUserName;
    /**
    * 调整部门ID
    */
    @TableField("adjustment_dept_id")
    private String adjustmentDeptId;
    /**
    * 调整部门名称
    */
    @TableField("adjustment_dept_name")
    private String adjustmentDeptName;
    /**
    * 调整日期
    */
    @TableField("adjustment_date")
    private LocalDate adjustmentDate;
    /**
    * 调整类型
    */
    @TableField("adjustment_type")
    private String adjustmentType;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 作废备注
    */
    @TableField("invalid_remark")
    private String invalidRemark;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String ADJUSTMENT_USER_ID = "adjustment_user_id";

    public static final String ADJUSTMENT_USER_NAME = "adjustment_user_name";

    public static final String ADJUSTMENT_DEPT_ID = "adjustment_dept_id";

    public static final String ADJUSTMENT_DEPT_NAME = "adjustment_dept_name";

    public static final String ADJUSTMENT_DATE = "adjustment_date";

    public static final String ADJUSTMENT_TYPE = "adjustment_type";

    public static final String REMARK = "remark";

    public static final String INVALID_REMARK = "invalid_remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}