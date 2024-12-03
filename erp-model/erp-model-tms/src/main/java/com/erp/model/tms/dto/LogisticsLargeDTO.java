package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.tms.enums.LogisticsLargeShippingMethodEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 物流大表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-11-29
*/
@Data
@NoArgsConstructor
public class LogisticsLargeDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 出库单号
        */
        private String outstockCode;

        /**
        * 出库时间
        */
        private LocalDateTime outstockTime;

        /**
        * 付款状态（待付款、已付款）
        */
        private String payStatus;

        /**
        * 应付账期（天）
        */
        private String payTermsDays;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编码
        */
        private String skuNo;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 平台订单号
        */
        private String platformOrderCode;

        /**
        * 重量
        */
        private BigDecimal weight;

        /**
        * 物流商计费重量
        */
        private BigDecimal logisticsBillingWeight;

        /**
        * 运输方式
        */
        private String shippingMethod;

        /**
        * 物流公司id
        */
        private String logisticsSupplierId;

        /**
        * 物流公司名称
        */
        private String logisticsSupplierName;

        /**
        * 付款公司名称
        */
        private String paymentCompanyName;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 起运地
        */
        private String originPort;

        /**
        * 中转地
        */
        private String transitPort;

        /**
        * 目的港
        */
        private String destinationPort;

        /**
        * 取件地址
        */
        private String pickupAddress;

        /**
        * 收件地址
        */
        private String deliveryAddress;

        /**
        * 取件时间
        */
        private LocalDateTime pickupTime;

        /**
        * 实际送达时间
        */
        private LocalDateTime actualDeliveryTime;

        /**
        * 计算系数运费
        */
        private BigDecimal freightCalculationFactor;

        /**
        * 运费币种
        */
        private String freightCurrency;

        /**
        * 账单总金额
        */
        private BigDecimal billTotalAmount;

        /**
        * 头程预估运费（含税）
        */
        private BigDecimal firstMileEstimatedFreightTax;

        /**
        * 头程预估运费（不含税）
        */
        private BigDecimal firstMileEstimatedFreight;

        /**
        * 头程实际运费（含税）
        */
        private BigDecimal firstMileActualFreightTax;

        /**
        * 头程实际运费（不含税）
        */
        private BigDecimal firstMileActualFreight;

        /**
        * 税率
        */
        private BigDecimal taxRate;

        /**
        * 头程运费增值税
        */
        private BigDecimal firstMileFreightVatAmount;

        /**
        * 头程付款时间
        */
        private LocalDateTime firstMilePayTime;

        /**
        * 尾程运费金额（含税）
        */
        private BigDecimal lastMileFreightAmountTax;

        /**
        * 尾程运费金额（不含税）
        */
        private BigDecimal lastMileFreightAmount;

        /**
        * 尾程运费金额-增值税
        */
        private BigDecimal lastMileFreightVatAmount;

        /**
        * 目的杂费计算系数
        */
        private BigDecimal destMiscFeeFactor;

        /**
        * 目的地杂费付款状态
        */
        private String destMiscFeePayStatus;

        /**
        * 杂费币别
        */
        private String miscFeeCurrency;

        /**
        * 预估目的港杂费
        */
        private BigDecimal estimatedDestMiscFee;

        /**
        * 实际目的港杂费
        */
        private BigDecimal actualDestMiscFee;

        /**
        * 目的港杂费付款时间
        */
        private LocalDateTime destMiscFeePayTime;

        /**
        * 目的杂费计算系数
        */
        private BigDecimal dutyCalculationFactor;

        /**
        * 目的地关税付款状态
        */
        private String destDutyPayStatus;

        /**
        * 关税币别
        */
        private String dutyCurrency;

        /**
        * 预估税金-关税
        */
        private BigDecimal estimatedDutyAmount;

        /**
        * 实际税金-关税
        */
        private BigDecimal actualDutyAmount;

        /**
        * 目的地税金付款时间
        */
        private LocalDateTime destTaxPayTime;

        /**
        * 可抵扣税金计算系数
        */
        private BigDecimal deductibleTaxFactor;

        /**
        * 可抵扣税金付款状态
        */
        private String deductibleTaxPayStatus;

        /**
        * 可抵扣税金币别
        */
        private String deductibleTaxCurrency;

        /**
        * 预估的可抵扣税金
        */
        private BigDecimal estimatedDeductibleTax;

        /**
        * 实际的可抵扣税金
        */
        private BigDecimal actualDeductibleTax;

        /**
        * 可抵扣税金付款时间
        */
        private LocalDateTime deductibleTaxPayTime;

        /**
        * 其他税金计算系数
        */
        private BigDecimal otherTaxCalculationFactor;

        /**
        * 其他税金付款状态
        */
        private String otherTaxPayStatus;

        /**
        * 其他税金币别
        */
        private String otherTaxCurrency;

        /**
        * 预估税金-其他税金
        */
        private BigDecimal estimatedTaxOtherTax;

        /**
        * 实际税金-其他税金
        */
        private BigDecimal actualTaxOtherTax;

        /**
        * 其他税金付款时间
        */
        private LocalDateTime otherTaxPayTime;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 出库单号
        */
        @NotBlank(message = "出库单号不能为空")
        @Size(max = 64,message = "出库单号最大长度不能超过64位")
        private String outstockCode;

        /**
        * 出库时间
        */
        private LocalDateTime outstockTime;

        /**
        * 付款状态（待付款、已付款）
        */
        @NotBlank(message = "付款状态（待付款、已付款）不能为空")
        @Size(max = 16,message = "付款状态（待付款、已付款）最大长度不能超过16位")
        private String payStatus;

        /**
        * 应付账期（天）
        */
        @NotBlank(message = "应付账期（天）不能为空")
        @Size(max = 64,message = "应付账期（天）最大长度不能超过64位")
        private String payTermsDays;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 32,message = "产品id最大长度不能超过32位")
        private String skuId;


        /**
        * 产品编码
        */
        @NotBlank(message = "产品编码不能为空")
        @Size(max = 32,message = "产品编码最大长度不能超过32位")
        private String skuNo;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 64,message = "平台订单号最大长度不能超过64位")
        private String platformOrderCode;

        /**
        * 重量
        */
        @NotNull(message = "重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal weight;

        /**
        * 物流商计费重量
        */
        @NotNull(message = "物流商计费重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "物流商计费重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal logisticsBillingWeight;

        /**
        * 运输方式
        */
        @NotBlank(message = "运输方式不能为空")
        @Size(max = 64,message = "运输方式最大长度不能超过64位")
        private String shippingMethod;

        /**
        * 物流公司id
        */
        @NotBlank(message = "物流公司id不能为空")
        @Size(max = 19,message = "物流公司id最大长度不能超过19位")
        private String logisticsSupplierId;

        /**
        * 物流公司名称
        */
        @NotBlank(message = "物流公司名称不能为空")
        @Size(max = 255,message = "物流公司名称最大长度不能超过255位")
        private String logisticsSupplierName;

        /**
        * 付款公司名称
        */
        @NotBlank(message = "付款公司名称不能为空")
        @Size(max = 200,message = "付款公司名称最大长度不能超过200位")
        private String paymentCompanyName;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 64,message = "运单号最大长度不能超过64位")
        private String transportNo;

        /**
        * 起运地
        */
        @NotBlank(message = "起运地不能为空")
        @Size(max = 500,message = "起运地最大长度不能超过500位")
        private String originPort;

        /**
        * 中转地
        */
        @NotBlank(message = "中转地不能为空")
        @Size(max = 500,message = "中转地最大长度不能超过500位")
        private String transitPort;

        /**
        * 目的港
        */
        @NotBlank(message = "目的港不能为空")
        @Size(max = 500,message = "目的港最大长度不能超过500位")
        private String destinationPort;

        /**
        * 取件地址
        */
        @NotBlank(message = "取件地址不能为空")
        @Size(max = 500,message = "取件地址最大长度不能超过500位")
        private String pickupAddress;

        /**
        * 收件地址
        */
        @NotBlank(message = "收件地址不能为空")
        @Size(max = 500,message = "收件地址最大长度不能超过500位")
        private String deliveryAddress;

        /**
        * 取件时间
        */
        private LocalDateTime pickupTime;

        /**
        * 实际送达时间
        */
        private LocalDateTime actualDeliveryTime;

        /**
        * 计算系数运费
        */
        @NotNull(message = "计算系数运费不能为空")
        @Digits(integer = 10, fraction = 6, message = "计算系数运费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal freightCalculationFactor;

        /**
        * 运费币种
        */
        @NotBlank(message = "运费币种不能为空")
        @Size(max = 8,message = "运费币种最大长度不能超过8位")
        private String freightCurrency;

        /**
        * 账单总金额
        */
        @NotNull(message = "账单总金额不能为空")
        @Digits(integer = 10, fraction = 6, message = "账单总金额整数位不能超过10位，小数位不能超过6位")
        private BigDecimal billTotalAmount;

        /**
        * 头程预估运费（含税）
        */
        @NotNull(message = "头程预估运费（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程预估运费（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileEstimatedFreightTax;

        /**
        * 头程预估运费（不含税）
        */
        @NotNull(message = "头程预估运费（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程预估运费（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileEstimatedFreight;

        /**
        * 头程实际运费（含税）
        */
        @NotNull(message = "头程实际运费（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程实际运费（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileActualFreightTax;

        /**
        * 头程实际运费（不含税）
        */
        @NotNull(message = "头程实际运费（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程实际运费（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileActualFreight;

        /**
        * 税率
        */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 10, fraction = 6, message = "税率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal taxRate;

        /**
        * 头程运费增值税
        */
        @NotNull(message = "头程运费增值税不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程运费增值税整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileFreightVatAmount;

        /**
        * 头程付款时间
        */
        private LocalDateTime firstMilePayTime;

        /**
        * 尾程运费金额（含税）
        */
        @NotNull(message = "尾程运费金额（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightAmountTax;

        /**
        * 尾程运费金额（不含税）
        */
        @NotNull(message = "尾程运费金额（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightAmount;

        /**
        * 尾程运费金额-增值税
        */
        @NotNull(message = "尾程运费金额不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightVatAmount;

        /**
        * 目的杂费计算系数
        */
        @NotNull(message = "目的杂费计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "目的杂费计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal destMiscFeeFactor;

        /**
        * 目的地杂费付款状态
        */
        @NotBlank(message = "目的地杂费付款状态不能为空")
        @Size(max = 16,message = "目的地杂费付款状态最大长度不能超过16位")
        private String destMiscFeePayStatus;

        /**
        * 杂费币别
        */
        @NotBlank(message = "杂费币别不能为空")
        @Size(max = 8,message = "杂费币别最大长度不能超过8位")
        private String miscFeeCurrency;

        /**
        * 预估目的港杂费
        */
        @NotNull(message = "预估目的港杂费不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估目的港杂费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDestMiscFee;

        /**
        * 实际目的港杂费
        */
        @NotNull(message = "实际目的港杂费不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际目的港杂费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDestMiscFee;

        /**
        * 目的港杂费付款时间
        */
        private LocalDateTime destMiscFeePayTime;

        /**
        * 目的杂费计算系数
        */
        @NotNull(message = "目的杂费计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "目的杂费计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal dutyCalculationFactor;

        /**
        * 目的地关税付款状态
        */
        @NotBlank(message = "目的地关税付款状态不能为空")
        @Size(max = 16,message = "目的地关税付款状态最大长度不能超过16位")
        private String destDutyPayStatus;

        /**
        * 关税币别
        */
        @NotBlank(message = "关税币别不能为空")
        @Size(max = 8,message = "关税币别最大长度不能超过8位")
        private String dutyCurrency;

        /**
        * 预估税金-关税
        */
        @NotNull(message = "预估税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDutyAmount;

        /**
        * 实际税金-关税
        */
        @NotNull(message = "实际税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDutyAmount;

        /**
        * 目的地税金付款时间
        */
        private LocalDateTime destTaxPayTime;

        /**
        * 可抵扣税金计算系数
        */
        @NotNull(message = "可抵扣税金计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "可抵扣税金计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal deductibleTaxFactor;

        /**
        * 可抵扣税金付款状态
        */
        @NotBlank(message = "可抵扣税金付款状态不能为空")
        @Size(max = 16,message = "可抵扣税金付款状态最大长度不能超过16位")
        private String deductibleTaxPayStatus;

        /**
        * 可抵扣税金币别
        */
        private String deductibleTaxCurrency;

        /**
        * 预估的可抵扣税金
        */
        @NotNull(message = "预估的可抵扣税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估的可抵扣税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDeductibleTax;

        /**
        * 实际的可抵扣税金
        */
        @NotNull(message = "实际的可抵扣税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际的可抵扣税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDeductibleTax;

        /**
        * 可抵扣税金付款时间
        */
        private LocalDateTime deductibleTaxPayTime;

        /**
        * 其他税金计算系数
        */
        @NotNull(message = "其他税金计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "其他税金计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal otherTaxCalculationFactor;

        /**
        * 其他税金付款状态
        */
        @NotBlank(message = "其他税金付款状态不能为空")
        @Size(max = 16,message = "其他税金付款状态最大长度不能超过16位")
        private String otherTaxPayStatus;

        /**
        * 其他税金币别
        */
        @NotBlank(message = "其他税金币别不能为空")
        @Size(max = 8,message = "其他税金币别最大长度不能超过8位")
        private String otherTaxCurrency;

        /**
        * 预估税金-其他税金
        */
        @NotNull(message = "预估税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedTaxOtherTax;

        /**
        * 实际税金-其他税金
        */
        @NotNull(message = "实际税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualTaxOtherTax;

        /**
        * 其他税金付款时间
        */
        private LocalDateTime otherTaxPayTime;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;
        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;
        /**
         * 来源详情id
         */
        @NotBlank(message = "来源详情id不能为空")
        private String sourceDetailId;
    }



    @Data
    @NoArgsConstructor
    public static class LargeDataDTO {

        /**
         * 出库单号
         */
        private String outstockCode;

        /**
         * 出库时间
         */
        private LocalDateTime outstockTime;

        /**
         * 付款状态（待付款、已付款）
         */
        private String payStatus;

        /**
         * 产品id
         */
        private String skuId;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 重量
         */
        private BigDecimal weight;

        /**
         * 物流商计费重量
         */
        private BigDecimal logisticsBillingWeight;

        /**
         * 运输方式
         */
        private String shippingMethod;

        /**
         * 物流公司id
         */
        private String logisticsSupplierId;

        /**
         * 物流公司名称
         */
        private String logisticsSupplierName;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 取件时间
         */
        private LocalDateTime pickupTime;

        /**
         * 实际送达时间
         */
        private LocalDateTime actualDeliveryTime;

        /**
         * 计算系数运费
         */
        @NotNull(message = "计算系数运费不能为空")
        @Digits(integer = 10, fraction = 6, message = "计算系数运费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal freightCalculationFactor;

        /**
         * 运费币种
         */
        @NotBlank(message = "运费币种不能为空")
        @Size(max = 8,message = "运费币种最大长度不能超过8位")
        private String freightCurrency;

        /**
         * 账单总金额
         */
        @NotNull(message = "账单总金额不能为空")
        @Digits(integer = 10, fraction = 6, message = "账单总金额整数位不能超过10位，小数位不能超过6位")
        private BigDecimal billTotalAmount;

        /**
         * 头程预估运费（含税）
         */
        @NotNull(message = "头程预估运费（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程预估运费（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileEstimatedFreightTax;

        /**
         * 头程预估运费（不含税）
         */
        @NotNull(message = "头程预估运费（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程预估运费（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileEstimatedFreight;

        /**
         * 头程实际运费（含税）
         */
        @NotNull(message = "头程实际运费（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程实际运费（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileActualFreightTax;

        /**
         * 头程实际运费（不含税）
         */
        @NotNull(message = "头程实际运费（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程实际运费（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileActualFreight;

        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        @Digits(integer = 10, fraction = 6, message = "税率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal taxRate;

        /**
         * 头程运费增值税
         */
        @NotNull(message = "头程运费增值税不能为空")
        @Digits(integer = 10, fraction = 6, message = "头程运费增值税整数位不能超过10位，小数位不能超过6位")
        private BigDecimal firstMileFreightVatAmount;

        /**
         * 头程付款时间
         */
        private LocalDateTime firstMilePayTime;

        /**
         * 尾程运费金额（含税）
         */
        @NotNull(message = "尾程运费金额（含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额（含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightAmountTax;

        /**
         * 尾程运费金额（不含税）
         */
        @NotNull(message = "尾程运费金额（不含税）不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额（不含税）整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightAmount;

        /**
         * 尾程运费金额-增值税
         */
        @NotNull(message = "尾程运费金额不能为空")
        @Digits(integer = 10, fraction = 6, message = "尾程运费金额整数位不能超过10位，小数位不能超过6位")
        private BigDecimal lastMileFreightVatAmount;

        /**
         * 目的杂费计算系数
         */
        @NotNull(message = "目的杂费计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "目的杂费计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal destMiscFeeFactor;

        /**
         * 目的地杂费付款状态
         */
        @NotBlank(message = "目的地杂费付款状态不能为空")
        @Size(max = 16,message = "目的地杂费付款状态最大长度不能超过16位")
        private String destMiscFeePayStatus;

        /**
         * 杂费币别
         */
        @NotBlank(message = "杂费币别不能为空")
        @Size(max = 8,message = "杂费币别最大长度不能超过8位")
        private String miscFeeCurrency;

        /**
         * 预估目的港杂费
         */
        @NotNull(message = "预估目的港杂费不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估目的港杂费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDestMiscFee;

        /**
         * 实际目的港杂费
         */
        @NotNull(message = "实际目的港杂费不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际目的港杂费整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDestMiscFee;

        /**
         * 目的港杂费付款时间
         */
        private LocalDateTime destMiscFeePayTime;

        /**
         * 目的杂费计算系数
         */
        @NotNull(message = "目的杂费计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "目的杂费计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal dutyCalculationFactor;

        /**
         * 目的地关税付款状态
         */
        @NotBlank(message = "目的地关税付款状态不能为空")
        @Size(max = 16,message = "目的地关税付款状态最大长度不能超过16位")
        private String destDutyPayStatus;

        /**
         * 关税币别
         */
        @NotBlank(message = "关税币别不能为空")
        @Size(max = 8,message = "关税币别最大长度不能超过8位")
        private String dutyCurrency;

        /**
         * 预估税金-关税
         */
        @NotNull(message = "预估税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDutyAmount;

        /**
         * 实际税金-关税
         */
        @NotNull(message = "实际税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDutyAmount;

        /**
         * 目的地税金付款时间
         */
        private LocalDateTime destTaxPayTime;

        /**
         * 可抵扣税金计算系数
         */
        @NotNull(message = "可抵扣税金计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "可抵扣税金计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal deductibleTaxFactor;

        /**
         * 可抵扣税金付款状态
         */
        @NotBlank(message = "可抵扣税金付款状态不能为空")
        @Size(max = 16,message = "可抵扣税金付款状态最大长度不能超过16位")
        private String deductibleTaxPayStatus;

        /**
         * 可抵扣税金币别
         */
        private String deductibleTaxCurrency;

        /**
         * 预估的可抵扣税金
         */
        @NotNull(message = "预估的可抵扣税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估的可抵扣税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedDeductibleTax;

        /**
         * 实际的可抵扣税金
         */
        @NotNull(message = "实际的可抵扣税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际的可抵扣税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualDeductibleTax;

        /**
         * 可抵扣税金付款时间
         */
        private LocalDateTime deductibleTaxPayTime;

        /**
         * 其他税金计算系数
         */
        @NotNull(message = "其他税金计算系数不能为空")
        @Digits(integer = 10, fraction = 6, message = "其他税金计算系数整数位不能超过10位，小数位不能超过6位")
        private BigDecimal otherTaxCalculationFactor;

        /**
         * 其他税金付款状态
         */
        @NotBlank(message = "其他税金付款状态不能为空")
        @Size(max = 16,message = "其他税金付款状态最大长度不能超过16位")
        private String otherTaxPayStatus;

        /**
         * 其他税金币别
         */
        @NotBlank(message = "其他税金币别不能为空")
        @Size(max = 8,message = "其他税金币别最大长度不能超过8位")
        private String otherTaxCurrency;

        /**
         * 预估税金-其他税金
         */
        @NotNull(message = "预估税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "预估税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal estimatedTaxOtherTax;

        /**
         * 实际税金-其他税金
         */
        @NotNull(message = "实际税金不能为空")
        @Digits(integer = 10, fraction = 6, message = "实际税金整数位不能超过10位，小数位不能超过6位")
        private BigDecimal actualTaxOtherTax;

        /**
         * 其他税金付款时间
         */
        private LocalDateTime otherTaxPayTime;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;
        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;
        /**
         * 来源详情id
         */
        @NotBlank(message = "来源详情id不能为空")
        private String sourceDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public class PagingViewDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 财务期间【可排序】
         */
        private LocalDate reconciliationMonth;

        /**
         * 出库单号【可排序】
         */
        private String outstockCode;

        /**
         * 出库时间【可排序】
         */
        private LocalDateTime outstockTime;

        /**
         * 付款状态（待付款、已付款）
         */
        @Dict(enumClass = SoB2cPayStatusEnum.class)
        private String payStatus;

        /**
         * 应付账期(天)
         */
        private String payTermsDays;

        /**
         * 物料id
         */
        private String skuId;

        /**
         * 物料编码
         */
        private String skuNo;

        /**
         * 物料名称
         */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 销售订单号【可排序】
         */
        private String platformOrderCode;

        /**
         * 重量【可排序】
         */
        private BigDecimal weight;

        /**
         * 物流商计费重量【可排序】
         */
        private BigDecimal logisticsBillingWeight;

        /**
         * 运输方式
         */
        @Dict(enumClass = LogisticsLargeShippingMethodEnum.class)
        private String shippingMethod;

        /**
         * 物流公司名称【可排序】
         */
        private String logisticsSupplierName;

        /**
         * 运单号【可排序】
         */
        private String transportNo;

        /**
         * 起运地【可排序】
         */
        private String originPort;

        /**
         * 中转地【可排序】
         */
        private String transitPort;

        /**
         * 目的港【可排序】
         */
        private String destinationPort;

        /**
         * 取件地址（详细地址）【可排序】
         */
        private String pickupAddress;

        /**
         * 收件地址（详细地址）【可排序】
         */
        private String deliveryAddress;

        /**
         * 取件时间【可排序】
         */
        private LocalDateTime pickupTime;

        /**
         * 实际送达时间【可排序】
         */
        private LocalDateTime actualDeliveryTime;

        /**
         * 运费计算系数【可排序】
         */
        private BigDecimal freightCalculationFactor;

        /**
         * 运费币种【可排序】
         */
        private String freightCurrency;

        /**
         * 头程预估运费（含税）【可排序】
         */
        private BigDecimal firstMileEstimatedFreightTax;

        /**
         * 头程预估运费（不含税）【可排序】
         */
        private BigDecimal firstMileEstimatedFreight;

        /**
         * 头程实际运费（含税）【可排序】
         */
        private BigDecimal firstMileActualFreightTax;

        /**
         * 头程实际运费（不含税）【可排序】
         */
        private BigDecimal firstMileActualFreight;

        /**
         * 税率【可排序】
         */
        private BigDecimal taxRate;

        /**
         * 头程运费增值税【可排序】
         */
        private BigDecimal firstMileFreightVatAmount;

        /**
         * 头程付款时间【可排序】
         */
        private LocalDateTime firstMilePayTime;

        /**
         * 尾程运费金额（含税）【可排序】
         */
        private BigDecimal lastMileFreightAmountTax;

        /**
         * 尾程运费金额（不含税）【可排序】
         */
        private BigDecimal lastMileFreightAmount;

        /**
         * 尾程运费金额-增值税【可排序】
         */
        private BigDecimal lastMileFreightVatAmount;

        /**
         * 目的杂费计算系数【可排序】
         */
        private BigDecimal destMiscFeeFactor;

        /**
         * 目的地杂费付款状态
         */
        @Dict(enumClass = LogisticsLargeShippingMethodEnum.class)
        private String destMiscFeePayStatus;

        /**
         * 杂费币别【可排序】
         */
        private String miscFeeCurrency;

        /**
         * 预估目的港杂费【可排序】
         */
        private BigDecimal estimatedDestMiscFee;

        /**
         * 实际目的港杂费【可排序】
         */
        private BigDecimal actualDestMiscFee;

        /**
         * 目的港杂费付款时间【可排序】
         */
        private LocalDateTime destMiscFeePayTime;

        /**
         * 目的杂费计算系数【可排序】
         */
        private BigDecimal dutyCalculationFactor;

        /**
         * 目的地关税付款状态
         */
        @Dict(enumClass = LogisticsLargeShippingMethodEnum.class)
        private String destDutyPayStatus;

        /**
         * 关税币别【可排序】
         */
        private String dutyCurrency;

        /**
         * 预估税金-关税【可排序】
         */
        private BigDecimal estimatedDutyAmount;

        /**
         * 实际税金-关税【可排序】
         */
        private BigDecimal actualDutyAmount;

        /**
         * 目的地税金付款时间【可排序】
         */
        private LocalDateTime destTaxPayTime;

        /**
         * 可抵扣税金计算系数【可排序】
         */
        private BigDecimal deductibleTaxFactor;

        /**
         * 可抵扣税金付款状态
         */
        @Dict(enumClass = LogisticsLargeShippingMethodEnum.class)
        private String deductibleTaxPayStatus;

        /**
         * 可抵扣税金币别【可排序】
         */
        private String deductibleTaxCurrency;

        /**
         * 预估的可抵扣税金【可排序】
         */
        private BigDecimal estimatedDeductibleTax;

        /**
         * 实际的可抵扣税金【可排序】
         */
        private BigDecimal actualDeductibleTax;

        /**
         * 可抵扣税金付款时间【可排序】
         */
        private LocalDateTime deductibleTaxPayTime;

        /**
         * 其他税金计算系数【可排序】
         */
        private BigDecimal otherTaxCalculationFactor;

        /**
         * 其他税金付款状态
         */
        @Dict(enumClass = LogisticsLargeShippingMethodEnum.class)
        private String otherTaxPayStatus;

        /**
         * 其他税金币别【可排序】
         */
        private String otherTaxCurrency;

        /**
         * 预估税金-其他税金【可排序】
         */
        private BigDecimal estimatedTaxOtherTax;

        /**
         * 实际税金-其他税金【可排序】
         */
        private BigDecimal actualTaxOtherTax;

        /**
         * 其他税金付款时间【可排序】
         */
        private LocalDateTime otherTaxPayTime;

        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源详情id
         */
        private String sourceDetailId;
        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 all全部
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends LogisticsLargeDTO.PagingParamDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
}