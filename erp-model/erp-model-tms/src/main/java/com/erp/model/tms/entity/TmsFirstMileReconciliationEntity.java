package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 头程对账单
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tms_first_mile_reconciliation")
public class TmsFirstMileReconciliationEntity extends BaseEntity<TmsFirstMileReconciliationEntity> {

    /**
     * 对账单号
     */
    @TableField("code")
    private String code;
    /**
     * 审核状态
     */
    @TableField("approve_status")
    private String approveStatus;
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
    @TableField("submit_date")
    private LocalDate submitDate;
    /**
     * 审核日期
     */
    @TableField("approve_date")
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
     * 费用合计
     */
    @TableField("total_cost")
    private BigDecimal totalCost;
    /**
     * 审核不通过原因
     */
    @TableField("reason")
    private String reason;
    /**
     * 对账月份（取值为对账周期末值所在月份）
     */
    @TableField("reconciliation_month")
    private LocalDate reconciliationMonth;
    /**
     * 付款状态（待付款、已付款）
     */
    @TableField("pay_status")
    private String payStatus;
    /**
     * 付款时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;
    /**
     * 供应商类型（logistics 物流对账单，warehouse仓储对账单，custom自定义物流商）
     * SupplierTypeEnum
     */
    @TableField("supplier_type")
    private String supplierType;


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

    public static final String TOTAL_COST = "total_cost";

    public static final String FIELD_REASON = "reason";

    public TmsFirstMileReconciliationEntity(String code, LocalDate startDate, LocalDate endDate, String logisticsSupplierId, String supplierType, String currency) {
        this.code = code;
        this.approveStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        this.reconciliationDate = LocalDate.now();
        //获取当前月份第一天
        this.reconciliationMonth = endDate.withDayOfMonth(1);
        this.startDate = startDate;
        this.endDate = endDate;
        this.logisticsSupplierId = logisticsSupplierId;
        this.supplierType = supplierType;
        this.logisticsSupplierName = "";
        this.currency = currency;
    }
}