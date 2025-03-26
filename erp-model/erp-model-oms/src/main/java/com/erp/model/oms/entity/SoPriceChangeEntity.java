package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 销售价变更表
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_price_change")
public class SoPriceChangeEntity extends BaseEntity<SoPriceChangeEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 变更原因
    */
    @TableField("reason")
    private String reason;
    /**
    * 审核状态 
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 调价日期
    */
    @TableField("adjust_date")
    private LocalDate adjustDate;
    /**
    * 调价人id
    */
    @TableField("adjust_user_id")
    private String adjustUserId;
    /**
    * 调价人
    */
    @TableField("adjust_user_name")
    private String adjustUserName;
    /**
    * 销售组织
    */
    @TableField("so_org_id")
    private String soOrgId;
    /**
    * 销售组织名
    */
    @TableField("so_org_name")
    private String soOrgName;
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


    public static final String CODE = "code";

    public static final String REASON = "reason";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String ADJUST_DATE = "adjust_date";

    public static final String ADJUST_USER_ID = "adjust_user_id";

    public static final String ADJUST_USER_NAME = "adjust_user_name";

    public static final String SO_ORG_ID = "so_org_id";

    public static final String SO_ORG_NAME = "so_org_name";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}