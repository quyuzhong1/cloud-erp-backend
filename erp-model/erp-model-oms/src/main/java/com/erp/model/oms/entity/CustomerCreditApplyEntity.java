package com.erp.model.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 客户授信
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("customer_credit_apply")
public class CustomerCreditApplyEntity extends BaseEntity<CustomerCreditApplyEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 客户Id
    */
    @TableField("customer_id")
    private String customerId;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人姓名
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 授信类型
    */
    @TableField("credit_type")
    private String creditType;
    /**
    * 授信状态
    */
    @TableField("credit_status")
    private String creditStatus;
    /**
    * 授信额度
    */
    @TableField("credit_amount")
    private BigDecimal creditAmount;
    /**
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 销售员
    */
    @TableField("sale_user_id")
    private String saleUserId;
    /**
    * 销售组织id
    */
    @TableField("sale_org_id")
    private String saleOrgId;
    /**
    * 账期
    */
    @TableField("period")
    private String period;
    /**
    * 贸易条款
    */
    @TableField("trade_term")
    private String tradeTerm;
    /**
    * 销售员部门
    */
    @TableField("sale_dept_id")
    private String saleDeptId;
    /**
    * 审核备注

    */
    @TableField("approve_remark")
    private String approveRemark;


    public static final String CODE = "code";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String CREDIT_TYPE = "credit_type";

    public static final String CREDIT_STATUS = "credit_status";

    public static final String CREDIT_AMOUNT = "credit_amount";

    public static final String CURRENCY = "currency";

    public static final String SALE_USER_ID = "sale_user_id";

    public static final String SALE_ORG_ID = "sale_org_id";

    public static final String PERIOD = "period";

    public static final String TRADE_TERM = "trade_term";

    public static final String SALE_DEPT_ID = "sale_dept_id";

    public static final String APPROVE_REMARK = "approve_remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}