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
 * 虚拟仓调整单主表
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_adjust")
public class VirtualAdjustEntity extends BaseEntity<VirtualAdjustEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态 
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
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
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String BILL_DATE = "bill_date";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}