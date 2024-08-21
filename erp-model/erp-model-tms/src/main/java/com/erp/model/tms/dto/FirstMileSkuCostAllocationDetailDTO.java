package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 头程费用SKU分摊明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class FirstMileSkuCostAllocationDetailDTO implements Serializable {




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
        * 主表id(first_mile_cost_allocation.id)
        */
        private String mainId;

        /**
        * sku分摊主表id(first_mile_sku_cost_allocation.id)
        */
        private String costMainId;

        /**
         * 费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用
         * DictCostCategoryEnum
         */
        private String feeType;
        /**
         * 费用类型名称
         */
        private String feeTypeName;

        /**
         * 费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊
         * CostAllocationEnum
         */
        private String allocationType;
        /**
         * 费用分摊方式名称
         */
        private String allocationTypeName;

        /**
        * 头程总金额
        */
        private BigDecimal amount;

        /**
        * 头程分摊金额
        */
        private BigDecimal allocatedAmount;

        /**
        * 单个产品分摊金额
        */
        private BigDecimal productAllocatedAmount;

        /**
        * 期初在途费用
        */
        private BigDecimal initTransitCost;

        /**
        * 期初暂估费用
        */
        private BigDecimal initEstimatedCost;

        /**
        * 冲期初在途费用
        */
        private BigDecimal midPeriodTransitCost;

        /**
        * 本期分摊费用
        */
        private BigDecimal currentPeriodAllocatedCost;

        /**
        * 期末在途费用
        */
        private BigDecimal endPeriodTransitCost;

        /**
        * 期末暂估费用
        */
        private BigDecimal endPeriodEstimatedCost;


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
        * 主表id(first_mile_cost_allocation.id)
        */
        @NotBlank(message = "主表id(first_mile_cost_allocation.id)不能为空")
        @Size(max = 19,message = "主表id(first_mile_cost_allocation.id)最大长度不能超过19位")
        private String mainId;

        /**
        * sku分摊主表id(first_mile_sku_cost_allocation.id)
        */
        @NotBlank(message = "sku分摊主表id(first_mile_sku_cost_allocation.id)不能为空")
        @Size(max = 19,message = "sku分摊主表id(first_mile_sku_cost_allocation.id)最大长度不能超过19位")
        private String costMainId;

        /**
        * 费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用
         * DictCostCategoryEnum
        */
        @NotBlank(message = "费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用不能为空")
        @Size(max = 20,message = "费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用最大长度不能超过20位")
        private String feeType;

        /**
        * 费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊
         * CostAllocationEnum
        */
        @NotBlank(message = "费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊不能为空")
        @Size(max = 20,message = "费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊最大长度不能超过20位")
        private String allocationType;

        /**
        * 头程总金额
        */
        @NotNull(message = "头程总金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "头程总金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 头程分摊金额
        */
        @NotNull(message = "头程分摊金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "头程分摊金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allocatedAmount;

        /**
        * 单个产品分摊金额
        */
        @NotNull(message = "单个产品分摊金额不能为空")
        @Digits(integer = 12, fraction = 6, message = "单个产品分摊金额整数位不能超过12位，小数位不能超过6位")
        private BigDecimal productAllocatedAmount;

        /**
        * 期初在途费用
        */
        @NotNull(message = "期初在途费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初在途费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initTransitCost;

        /**
        * 期初暂估费用
        */
        @NotNull(message = "期初暂估费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期初暂估费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal initEstimatedCost;

        /**
        * 冲期初在途费用
        */
        @NotNull(message = "冲期初在途费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "冲期初在途费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal midPeriodTransitCost;

        /**
        * 本期分摊费用
        */
        @NotNull(message = "本期分摊费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "本期分摊费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal currentPeriodAllocatedCost;

        /**
        * 期末在途费用
        */
        @NotNull(message = "期末在途费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期末在途费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal endPeriodTransitCost;

        /**
        * 期末暂估费用
        */
        @NotNull(message = "期末暂估费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "期末暂估费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal endPeriodEstimatedCost;


    }


}