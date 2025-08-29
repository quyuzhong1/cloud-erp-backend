package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 收款单
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_receipt")
public class SoReceiptEntity extends BaseEntity<SoReceiptEntity> {

    /**
    * 单据编码
    */
    @TableField("code")
    private String code;
    /**
    * 第三方单据编号
    */
    @TableField("third_code")
    private String thirdCode;
    /**
     * 第三方系统
     */
    @TableField("third_system")
    private String thirdSystem;
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
    * 币种
    */
    @TableField("currency")
    private String currency;
    /**
    * 是否入账
    */
    @TableField("is_posted")
    private Boolean isPosted;
    /**
    * 入账账户
    */
    @TableField("posted_account")
    private String postedAccount;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源Id
    */
    @TableField("source_id")
    private String sourceId;
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
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
     * 收款金额
     */
    @TableField("receipt_amount")
    private BigDecimal receiptAmount;

    public static final String CODE = "code";

    public static final String THIRD_CODE = "third_code";

    public static final String CUSTOMER_ID = "customer_id";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String CURRENCY = "currency";

    public static final String IS_POSTED = "is_posted";

    public static final String POSTED_ACCOUNT = "posted_account";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_ID = "source_id";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}