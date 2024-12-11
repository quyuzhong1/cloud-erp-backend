package com.erp.model.tms.entity;

import java.math.BigDecimal;
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
 * 物流大表
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_large")
public class LogisticsLargeEntity extends BaseEntity<LogisticsLargeEntity> {

    /**
    * 出库单号
    */
    @TableField("outstock_code")
    private String outstockCode;
    /**
    * 出库时间
    */
    @TableField("outstock_time")
    private LocalDateTime outstockTime;
    /**
    * 付款状态（待付款、已付款）
    */
    @TableField("pay_status")
    private String payStatus;
    /**
    * 应付账期（天）
    */
    @TableField("pay_terms_days")
    private String payTermsDays;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 平台订单号
    */
    @TableField("platform_order_code")
    private String platformOrderCode;
    /**
    * 重量
    */
    @TableField("weight")
    private BigDecimal weight;
    /**
    * 物流商计费重量
    */
    @TableField("logistics_billing_weight")
    private BigDecimal logisticsBillingWeight;
    /**
    * 运输方式
    */
    @TableField("shipping_method")
    private String shippingMethod;
    /**
    * 物流公司id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流公司名称
    */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
    * 付款公司名称
    */
    @TableField("payment_company_name")
    private String paymentCompanyName;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 起运地
    */
    @TableField("origin_port")
    private String originPort;
    /**
    * 中转地
    */
    @TableField("transit_port")
    private String transitPort;
    /**
    * 目的港
    */
    @TableField("destination_port")
    private String destinationPort;
    /**
    * 取件地址
    */
    @TableField("pickup_address")
    private String pickupAddress;
    /**
    * 收件地址
    */
    @TableField("delivery_address")
    private String deliveryAddress;
    /**
    * 取件时间
    */
    @TableField("pickup_time")
    private LocalDateTime pickupTime;
    /**
    * 实际送达时间
    */
    @TableField("actual_delivery_time")
    private LocalDateTime actualDeliveryTime;
    /**
    * 计算系数运费
    */
    @TableField("freight_calculation_factor")
    private BigDecimal freightCalculationFactor;
    /**
    * 运费币种
    */
    @TableField("freight_currency")
    private String freightCurrency;
    /**
    * 账单总金额
    */
    @TableField("bill_total_amount")
    private BigDecimal billTotalAmount;
    /**
    * 头程预估运费（含税）
    */
    @TableField("first_mile_estimated_freight_tax")
    private BigDecimal firstMileEstimatedFreightTax;
    /**
    * 头程预估运费（不含税）
    */
    @TableField("first_mile_estimated_freight")
    private BigDecimal firstMileEstimatedFreight;
    /**
    * 头程实际运费（含税）
    */
    @TableField("first_mile_actual_freight_tax")
    private BigDecimal firstMileActualFreightTax;
    /**
    * 头程实际运费（不含税）
    */
    @TableField("first_mile_actual_freight")
    private BigDecimal firstMileActualFreight;
    /**
    * 税率
    */
    @TableField("tax_rate")
    private BigDecimal taxRate;
    /**
    * 头程运费增值税
    */
    @TableField("first_mile_freight_vat_amount")
    private BigDecimal firstMileFreightVatAmount;
    /**
    * 头程付款时间
    */
    @TableField("first_mile_pay_time")
    private LocalDateTime firstMilePayTime;
    /**
    * 尾程运费金额（含税）
    */
    @TableField("last_mile_freight_amount_tax")
    private BigDecimal lastMileFreightAmountTax;
    /**
    * 尾程运费金额（不含税）
    */
    @TableField("last_mile_freight_amount")
    private BigDecimal lastMileFreightAmount;
    /**
    * 尾程运费金额-增值税
    */
    @TableField("last_mile_freight_vat_amount")
    private BigDecimal lastMileFreightVatAmount;
    /**
    * 目的杂费计算系数
    */
    @TableField("dest_misc_fee_factor")
    private BigDecimal destMiscFeeFactor;
    /**
    * 目的地杂费付款状态
    */
    @TableField("dest_misc_fee_pay_status")
    private String destMiscFeePayStatus;
    /**
    * 杂费币别
    */
    @TableField("misc_fee_currency")
    private String miscFeeCurrency;
    /**
    * 预估目的港杂费
    */
    @TableField("estimated_dest_misc_fee")
    private BigDecimal estimatedDestMiscFee;
    /**
    * 实际目的港杂费
    */
    @TableField("actual_dest_misc_fee")
    private BigDecimal actualDestMiscFee;
    /**
    * 目的港杂费付款时间
    */
    @TableField("dest_misc_fee_pay_time")
    private LocalDateTime destMiscFeePayTime;
    /**
    * 目的杂费计算系数
    */
    @TableField("duty_calculation_factor")
    private BigDecimal dutyCalculationFactor;
    /**
    * 目的地关税付款状态
    */
    @TableField("dest_duty_pay_status")
    private String destDutyPayStatus;
    /**
    * 关税币别
    */
    @TableField("duty_currency")
    private String dutyCurrency;
    /**
    * 预估税金-关税
    */
    @TableField("estimated_duty_amount")
    private BigDecimal estimatedDutyAmount;
    /**
    * 实际税金-关税
    */
    @TableField("actual_duty_amount")
    private BigDecimal actualDutyAmount;
    /**
    * 目的地税金付款时间
    */
    @TableField("dest_tax_pay_time")
    private LocalDateTime destTaxPayTime;
    /**
    * 可抵扣税金计算系数
    */
    @TableField("deductible_tax_factor")
    private BigDecimal deductibleTaxFactor;
    /**
    * 可抵扣税金付款状态
    */
    @TableField("deductible_tax_pay_status")
    private String deductibleTaxPayStatus;
    /**
    * 可抵扣税金币别
    */
    @TableField("deductible_tax_currency")
    private String deductibleTaxCurrency;
    /**
    * 预估的可抵扣税金
    */
    @TableField("estimated_deductible_tax")
    private BigDecimal estimatedDeductibleTax;
    /**
    * 实际的可抵扣税金
    */
    @TableField("actual_deductible_tax")
    private BigDecimal actualDeductibleTax;
    /**
    * 可抵扣税金付款时间
    */
    @TableField("deductible_tax_pay_time")
    private LocalDateTime deductibleTaxPayTime;
    /**
    * 其他税金计算系数
    */
    @TableField("other_tax_calculation_factor")
    private BigDecimal otherTaxCalculationFactor;
    /**
    * 其他税金付款状态
    */
    @TableField("other_tax_pay_status")
    private String otherTaxPayStatus;
    /**
    * 其他税金币别
    */
    @TableField("other_tax_currency")
    private String otherTaxCurrency;
    /**
    * 预估税金-其他税金
    */
    @TableField("estimated_tax_other_tax")
    private BigDecimal estimatedTaxOtherTax;
    /**
    * 实际税金-其他税金
    */
    @TableField("actual_tax_other_tax")
    private BigDecimal actualTaxOtherTax;
    /**
    * 其他税金付款时间
    */
    @TableField("other_tax_pay_time")
    private LocalDateTime otherTaxPayTime;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源详情id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 对账单类型：estimated=预估账单，actual=实际账单
    * ReconciliationBillTypeEnum
    */
    @TableField("reconciliation_bill_type")
    private String reconciliationBillType;
    /**
     * 财务期间
     */
    @TableField("reconciliation_month")
    private LocalDate reconciliationMonth;
    /**
     * 是否是对冲预估账单的数据标识
     */
    @TableField("is_hedging")
    private Boolean isHedging;


    public static final String OUTSTOCK_CODE = "outstock_code";

    public static final String OUTSTOCK_TIME = "outstock_time";

    public static final String PAY_STATUS = "pay_status";

    public static final String PAY_TERMS_DAYS = "pay_terms_days";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String PLATFORM_ORDER_CODE = "platform_order_code";

    public static final String WEIGHT = "weight";

    public static final String LOGISTICS_BILLING_WEIGHT = "logistics_billing_weight";

    public static final String SHIPPING_METHOD = "shipping_method";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String PAYMENT_COMPANY_NAME = "payment_company_name";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String ORIGIN_PORT = "origin_port";

    public static final String TRANSIT_PORT = "transit_port";

    public static final String DESTINATION_PORT = "destination_port";

    public static final String PICKUP_ADDRESS = "pickup_address";

    public static final String DELIVERY_ADDRESS = "delivery_address";

    public static final String PICKUP_TIME = "pickup_time";

    public static final String ACTUAL_DELIVERY_TIME = "actual_delivery_time";

    public static final String FREIGHT_CALCULATION_FACTOR = "freight_calculation_factor";

    public static final String FREIGHT_CURRENCY = "freight_currency";

    public static final String BILL_TOTAL_AMOUNT = "bill_total_amount";

    public static final String FIRST_MILE_ESTIMATED_FREIGHT_TAX = "first_mile_estimated_freight_tax";

    public static final String FIRST_MILE_ESTIMATED_FREIGHT = "first_mile_estimated_freight";

    public static final String FIRST_MILE_ACTUAL_FREIGHT_TAX = "first_mile_actual_freight_tax";

    public static final String FIRST_MILE_ACTUAL_FREIGHT = "first_mile_actual_freight";

    public static final String TAX_RATE = "tax_rate";

    public static final String FIRST_MILE_FREIGHT_VAT_AMOUNT = "first_mile_freight_vat_amount";

    public static final String FIRST_MILE_PAY_TIME = "first_mile_pay_time";

    public static final String LAST_MILE_FREIGHT_AMOUNT_TAX = "last_mile_freight_amount_tax";

    public static final String LAST_MILE_FREIGHT_AMOUNT = "last_mile_freight_amount";

    public static final String LAST_MILE_FREIGHT_VAT_AMOUNT = "last_mile_freight_vat_amount";

    public static final String DEST_MISC_FEE_FACTOR = "dest_misc_fee_factor";

    public static final String DEST_MISC_FEE_PAY_STATUS = "dest_misc_fee_pay_status";

    public static final String MISC_FEE_CURRENCY = "misc_fee_currency";

    public static final String ESTIMATED_DEST_MISC_FEE = "estimated_dest_misc_fee";

    public static final String ACTUAL_DEST_MISC_FEE = "actual_dest_misc_fee";

    public static final String DEST_MISC_FEE_PAY_TIME = "dest_misc_fee_pay_time";

    public static final String DUTY_CALCULATION_FACTOR = "duty_calculation_factor";

    public static final String DEST_DUTY_PAY_STATUS = "dest_duty_pay_status";

    public static final String DUTY_CURRENCY = "duty_currency";

    public static final String ESTIMATED_DUTY_AMOUNT = "estimated_duty_amount";

    public static final String ACTUAL_DUTY_AMOUNT = "actual_duty_amount";

    public static final String DEST_TAX_PAY_TIME = "dest_tax_pay_time";

    public static final String DEDUCTIBLE_TAX_FACTOR = "deductible_tax_factor";

    public static final String DEDUCTIBLE_TAX_PAY_STATUS = "deductible_tax_pay_status";

    public static final String DEDUCTIBLE_TAX_CURRENCY = "deductible_tax_currency";

    public static final String ESTIMATED_DEDUCTIBLE_TAX = "estimated_deductible_tax";

    public static final String ACTUAL_DEDUCTIBLE_TAX = "actual_deductible_tax";

    public static final String DEDUCTIBLE_TAX_PAY_TIME = "deductible_tax_pay_time";

    public static final String OTHER_TAX_CALCULATION_FACTOR = "other_tax_calculation_factor";

    public static final String OTHER_TAX_PAY_STATUS = "other_tax_pay_status";

    public static final String OTHER_TAX_CURRENCY = "other_tax_currency";

    public static final String ESTIMATED_TAX_OTHER_TAX = "estimated_tax_other_tax";

    public static final String ACTUAL_TAX_OTHER_TAX = "actual_tax_other_tax";

    public static final String OTHER_TAX_PAY_TIME = "other_tax_pay_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}