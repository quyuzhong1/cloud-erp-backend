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
 * 样品转移单主表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_transfer_info")
public class SampleTransferInfoEntity extends BaseEntity<SampleTransferInfoEntity> {

    /**
    * 转移单号
    */
    @TableField("code")
    private String code;
    /**
    * 审批状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审批完成时间
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
    * 转出人ID
    */
    @TableField("transfer_out_user_id")
    private String transferOutUserId;
    /**
    * 转出人姓名
    */
    @TableField("transfer_out_user_name")
    private String transferOutUserName;
    /**
    * 转出人部门ID
    */
    @TableField("transfer_out_dept_id")
    private String transferOutDeptId;
    /**
    * 转出人部门名称
    */
    @TableField("transfer_out_dept_name")
    private String transferOutDeptName;
    /**
    * 转入人ID
    */
    @TableField("transfer_in_user_id")
    private String transferInUserId;
    /**
    * 转入人姓名
    */
    @TableField("transfer_in_user_name")
    private String transferInUserName;
    /**
    * 转入人部门ID
    */
    @TableField("transfer_in_dept_id")
    private String transferInDeptId;
    /**
    * 转入人部门名称
    */
    @TableField("transfer_in_dept_name")
    private String transferInDeptName;
    /**
    * 转移日期
    */
    @TableField("transfer_date")
    private LocalDate transferDate;
    /**
    * 备注说明
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

    public static final String TRANSFER_OUT_USER_ID = "transfer_out_user_id";

    public static final String TRANSFER_OUT_USER_NAME = "transfer_out_user_name";

    public static final String TRANSFER_OUT_DEPT_ID = "transfer_out_dept_id";

    public static final String TRANSFER_OUT_DEPT_NAME = "transfer_out_dept_name";

    public static final String TRANSFER_IN_USER_ID = "transfer_in_user_id";

    public static final String TRANSFER_IN_USER_NAME = "transfer_in_user_name";

    public static final String TRANSFER_IN_DEPT_ID = "transfer_in_dept_id";

    public static final String TRANSFER_IN_DEPT_NAME = "transfer_in_dept_name";

    public static final String TRANSFER_DATE = "transfer_date";

    public static final String REMARK = "remark";

    public static final String INVALID_REMARK = "invalid_remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}