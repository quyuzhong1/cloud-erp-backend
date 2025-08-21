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
 * 样品退回单
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_back_info")
public class SampleBackInfoEntity extends BaseEntity<SampleBackInfoEntity> {

    /**
    * 审批状态(waitSubmit=待提交, approved=已批准, rejected=已驳回)
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
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
    * 审批时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 作废状态(false:有效,true:已作废)
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 作废时间
    */
    @TableField("invalid_time")
    private LocalDateTime invalidTime;
    /**
    * 样品退回单号
    */
    @TableField("code")
    private String code;
    /**
    * 单据状态
    */
    @TableField("status")
    private String status;
    /**
    * 执行状态
    */
    @TableField("exec_status")
    private String execStatus;
    @TableField("back_date")
    private LocalDate backDate;
    /**
    * 来源ID（关联样品领用单）
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号（样品领用单号）
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 退回人ID
    */
    @TableField("user_id")
    private String userId;
    /**
    * 退回人姓名
    */
    @TableField("user_name")
    private String userName;
    /**
    * 退回部门ID
    */
    @TableField("dept_id")
    private String deptId;
    /**
    * 收货仓库ID
    */
    @TableField("warehouse_id")
    private String warehouseId;
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String INVALID_TIME = "invalid_time";

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String EXEC_STATUS = "exec_status";

    public static final String BACK_DATE = "back_date";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String DEPT_ID = "dept_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}