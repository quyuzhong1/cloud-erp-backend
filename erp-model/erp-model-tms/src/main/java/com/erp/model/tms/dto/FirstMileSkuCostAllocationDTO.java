package com.erp.model.tms.dto;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 头程费用SKU分摊请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class FirstMileSkuCostAllocationDTO implements Serializable {




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
        * skuId
        */
        private String skuId;

        /**
        * skuNO
        */
        private String skuNo;

        /**
        * 平台skuId
        */
        private String platformSkuId;

        /**
        * 平台skuNo
        */
        private String platformSkuNo;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 分摊重量
        */
        private BigDecimal allocatedWeight;

        /**
        * 单位成本
        */
        private BigDecimal productCost;

        /**
        * 产品总成本
        */
        private BigDecimal productTotalCost;

        /**
        * 期初签收数量
        */
        private Integer initReceiveQty;

        /**
        * 上月签收数量
        */
        private Integer lastMonthReceiveQty;

        /**
        * 本月签收数量
        */
        private Integer currentMonthReceiveQty;
        /**
         * 截止本月签收数量
         */
        private Integer asCurrentMonthReceiveQty;

        /**
        * 截止上月签收数量
        */
        private Integer asLastMonthReceiveQty;

        /**
        * 重量单位（默认kg）
        */
        private String weightUnit;

        /**
        * 币种（默认CNY）
        */
        private String currency;

        /**
        * 费用来源：estimatedBill=预估账单，actualBill=实际账单
        */
        private String billSourceType;


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
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 平台skuId
        */
        @NotBlank(message = "平台skuId不能为空")
        @Size(max = 19,message = "平台skuId最大长度不能超过19位")
        private String platformSkuId;

        /**
        * 平台skuNo
        */
        @NotBlank(message = "平台skuNo不能为空")
        @Size(max = 255,message = "平台skuNo最大长度不能超过255位")
        private String platformSkuNo;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 分摊重量
        */
        @NotNull(message = "分摊重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "分摊重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allocatedWeight;

        /**
        * 单位成本
        */
        @NotNull(message = "单位成本不能为空")
        @Digits(integer = 12, fraction = 6, message = "单位成本整数位不能超过12位，小数位不能超过6位")
        private BigDecimal productCost;

        /**
        * 产品总成本
        */
        @NotNull(message = "产品总成本不能为空")
        @Digits(integer = 12, fraction = 6, message = "产品总成本整数位不能超过12位，小数位不能超过6位")
        private BigDecimal productTotalCost;

        /**
        * 期初签收数量
        */
        @NotNull(message = "期初签收数量不能为空")
        private Integer initReceiveQty;

        /**
        * 上月签收数量
        */
        @NotNull(message = "上月签收数量不能为空")
        private Integer lastMonthReceiveQty;

        /**
        * 本月签收数量
        */
        @NotNull(message = "本月签收数量不能为空")
        private Integer currentMonthReceiveQty;
        /**
         * 截止本月签收数量
         */
        @NotNull(message = "截止本月签收数量不能为空")
        private Integer asCurrentMonthReceiveQty;
        /**
        * 截止上月签收数量
        */
        @NotNull(message = "截止上月签收数量不能为空")
        private Integer asLastMonthReceiveQty;

        /**
        * 重量单位（默认kg）
        */
        @NotBlank(message = "重量单位（默认kg）不能为空")
        @Size(max = 20,message = "重量单位（默认kg）最大长度不能超过20位")
        private String weightUnit;

        /**
        * 币种（默认CNY）
        */
        @NotBlank(message = "币种（默认CNY）不能为空")
        @Size(max = 20,message = "币种（默认CNY）最大长度不能超过20位")
        private String currency;

        /**
        * 费用来源：estimatedBill=预估账单，actualBill=实际账单
        */
        @NotBlank(message = "费用来源：estimatedBill=预估账单，actualBill=实际账单不能为空")
        @Size(max = 20,message = "费用来源：estimatedBill=预估账单，actualBill=实际账单最大长度不能超过20位")
        private String billSourceType;


    }


}