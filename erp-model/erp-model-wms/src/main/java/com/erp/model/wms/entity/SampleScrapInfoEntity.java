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
 * 样品报废单主表
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sample_scrap_info")
public class SampleScrapInfoEntity extends BaseEntity<SampleScrapInfoEntity> {

    /**
    * 报废单编号
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
    * 作废状态
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 报废操作人ID
    */
    @TableField("scrap_user_id")
    private String scrapUserId;
    /**
    * 报废操作人姓名
    */
    @TableField("scrap_user_name")
    private String scrapUserName;
    /**
    * 报废人部门ID
    */
    @TableField("scrap_dept_id")
    private String scrapDeptId;
    /**
    * 报废人部门名称
    */
    @TableField("scrap_dept_name")
    private String scrapDeptName;
    /**
    * 报废日期
    */
    @TableField("scrap_date")
    private LocalDate scrapDate;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String SCRAP_USER_ID = "scrap_user_id";

    public static final String SCRAP_USER_NAME = "scrap_user_name";

    public static final String SCRAP_DEPT_ID = "scrap_dept_id";

    public static final String SCRAP_DEPT_NAME = "scrap_dept_name";

    public static final String SCRAP_DATE = "scrap_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}