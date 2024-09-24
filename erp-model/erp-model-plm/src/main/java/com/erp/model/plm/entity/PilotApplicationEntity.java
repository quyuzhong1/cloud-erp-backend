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
 * 试产/量产申请
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pilot_application")
public class PilotApplicationEntity extends BaseEntity<PilotApplicationEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 订单状态
    */
    @TableField("order_status")
    private String orderStatus;
    /**
    * 单据备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 最新审核人ID
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String ORDER_STATUS = "order_status";

    public static final String REMARK = "remark";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}