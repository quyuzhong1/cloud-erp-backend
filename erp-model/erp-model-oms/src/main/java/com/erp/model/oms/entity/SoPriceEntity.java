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
 * 销售价目表
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_price")
public class SoPriceEntity extends BaseEntity<SoPriceEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 客户id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 报价日期
    */
    @TableField("quoted_date")
    private LocalDate quotedDate;
    /**
    * 审核状态 
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 定价人id
    */
    @TableField("pricing_user_id")
    private String pricingUserId;
    /**
    * 定价人
    */
    @TableField("pricing_user_name")
    private String pricingUserName;
    /**
    * 销售组织id
    */
    @TableField("so_org_id")
    private String soOrgId;
    /**
    * 销售组织名称
    */
    @TableField("so_org_name")
    private String soOrgName;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
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

    public static final String CUSTOMER_ID = "customer_id";

    public static final String QUOTED_DATE = "quoted_date";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String PRICING_USER_ID = "pricing_user_id";

    public static final String PRICING_USER_NAME = "pricing_user_name";

    public static final String SO_ORG_ID = "so_org_id";

    public static final String SO_ORG_NAME = "so_org_name";

    public static final String CURRENCY = "currency";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}