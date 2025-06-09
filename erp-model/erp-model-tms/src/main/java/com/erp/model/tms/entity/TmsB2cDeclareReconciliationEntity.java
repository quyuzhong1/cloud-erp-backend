package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * b2c报关对账单
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tms_b2c_declare_reconciliation")
public class TmsB2cDeclareReconciliationEntity extends BaseEntity<TmsB2cDeclareReconciliationEntity> {

    /**
    * 对账单号
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private ApproveStatusEnum approveStatus;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人名称
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 生成对账日期
    */
    @TableField("reconciliation_date")
    private LocalDate reconciliationDate;
    /**
    * 提交日期
    */
    @TableField(value = "submit_date",updateStrategy = FieldStrategy.IGNORED)
    private LocalDate submitDate;
    /**
    * 审核日期
    */
    @TableField(value = "approve_date",updateStrategy = FieldStrategy.IGNORED)
    private LocalDate approveDate;
    /**
    * 对账开始日期
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 对账结束日期
    */
    @TableField("end_date")
    private LocalDate endDate;
    /**
    * 物流商Id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流商名称
    */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
    * 币别
    */
    @TableField("currency")
    private String currency;
    /**
    * 汇率
    */
    @TableField("exchange_rate")
    private BigDecimal exchangeRate;
    /**
    * 审核不通过原因
    */
    @TableField("reason")
    private String reason;
    
    /**
     * 东莞仓费用
     */
    @TableField("dg_warsehouse_fee")
    private BigDecimal dgWarseHouseFee;
    
    /**
     * 香港仓费用
     */
    @TableField("xg_warsehouse_fee")
    private BigDecimal xgWarseHouseFee;
    
    /**
     * 支付状态
     */
    @TableField("pay_status")
    private String payStatus;
    
    /**
     * 付款时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;


    public static final String FIELD_CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String RECONCILIATION_DATE = "reconciliation_date";

    public static final String SUBMIT_DATE = "submit_date";

    public static final String APPROVE_DATE = "approve_date";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String FIELD_CURRENCY = "currency";

    public static final String EXCHANGE_RATE = "exchange_rate";

    public static final String FIELD_REASON = "reason";

    @Override
    public Serializable pkVal() {
        return null;
    }

}