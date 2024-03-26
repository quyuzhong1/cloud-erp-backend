package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 头程对账单明细请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
*/
@Data
@NoArgsConstructor
public class TmsFirstMileReconciliationDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 货件/计划单号
        */
        private String businessCode;

        /**
        * 店铺ID
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 发货国家代号
        */
        private String fromCountry;

        /**
        * 发货国家代号
        */
        private String toCountry;

        /**
        * 签收日期
        */
        private LocalDate reveiveDate;

        /**
        * 计费方式
        */
        private String billingMethod;

        /**
        * 计费方式名称
        */
        private String billingMethodName;

        /**
        * 类型(对账类型)
        */
        private String type;

        /**
        * 总物流费用
        */
        private BigDecimal totalLogisticsCost;

        /**
        * 实际重量
        */
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        private String actualWeightUnit;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 体积重单位
        */
        private String volumeWeightUnit;

        /**
        * 计费重
        */
        private BigDecimal billingWeight;

        /**
        * 计费重单位
        */
        private String billingWeightUnit;

        /**
        * 实际物流运费用
        */
        private BigDecimal actualShippingCost;

        /**
        * 实际报关费用
        */
        private BigDecimal actualDeclareCost;

        /**
        * 实际其他费用
        */
        private BigDecimal actualOtherCost;

        /**
        * 备注
        */
        private String remark;

        /**
        * 对账状态
        */
        private String status;

        /**
        * 确认时间
        */
        private LocalDate confirmDate;

        /**
        * 确认人id
        */
        private String confirmUserId;

        /**
        * 确认人名称
        */
        private String confirmUserName;


    }

    /**
    * 新增
    */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @EqualsAndHashCode(callSuper = true)
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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 32,message = "来源编码最大长度不能超过32位")
        private String sourceCode;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 19,message = "物流跟踪号最大长度不能超过19位")
        private String trackNo;

        /**
        * 货件/计划单号
        */
        @NotBlank(message = "货件/计划单号不能为空")
        @Size(max = 19,message = "货件/计划单号最大长度不能超过19位")
        private String businessCode;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 32,message = "店铺ID最大长度不能超过32位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 64,message = "店铺名称最大长度不能超过64位")
        private String shopName;

        /**
        * 发货国家代号
        */
        @NotBlank(message = "发货国家代号不能为空")
        @Size(max = 32,message = "发货国家代号最大长度不能超过32位")
        private String fromCountry;

        /**
        * 发货国家代号
        */
        @NotBlank(message = "发货国家代号不能为空")
        @Size(max = 32,message = "发货国家代号最大长度不能超过32位")
        private String toCountry;

        /**
        * 签收日期
        */
        private LocalDate reveiveDate;

        /**
        * 计费方式
        */
        @NotBlank(message = "计费方式不能为空")
        @Size(max = 32,message = "计费方式最大长度不能超过32位")
        private String billingMethod;

        /**
        * 计费方式名称
        */
        @NotBlank(message = "计费方式名称不能为空")
        @Size(max = 64,message = "计费方式名称最大长度不能超过64位")
        private String billingMethodName;

        /**
        * 类型(对账类型)
        */
        @NotBlank(message = "类型(对账类型)不能为空")
        @Size(max = 32,message = "类型(对账类型)最大长度不能超过32位")
        private String type;

        /**
        * 总物流费用
        */
        @NotNull(message = "总物流费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "总物流费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalLogisticsCost;

        /**
        * 实际重量
        */
        @NotNull(message = "实际重量不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际重量整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualWeight;

        /**
        * 实际重量单位
        */
        @NotBlank(message = "实际重量单位不能为空")
        @Size(max = 32,message = "实际重量单位最大长度不能超过32位")
        private String actualWeightUnit;

        /**
        * 体积重
        */
        @NotNull(message = "体积重不能为空")
        @Digits(integer = 12, fraction = 4, message = "体积重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal volumeWeight;

        /**
        * 体积重单位
        */
        @NotBlank(message = "体积重单位不能为空")
        @Size(max = 32,message = "体积重单位最大长度不能超过32位")
        private String volumeWeightUnit;

        /**
        * 计费重
        */
        @NotNull(message = "计费重不能为空")
        @Digits(integer = 12, fraction = 4, message = "计费重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal billingWeight;

        /**
        * 计费重单位
        */
        @NotBlank(message = "计费重单位不能为空")
        @Size(max = 32,message = "计费重单位最大长度不能超过32位")
        private String billingWeightUnit;

        /**
        * 实际物流运费用
        */
        @NotNull(message = "实际物流运费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际物流运费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualShippingCost;

        /**
        * 实际报关费用
        */
        @NotNull(message = "实际报关费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际报关费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualDeclareCost;

        /**
        * 实际其他费用
        */
        @NotNull(message = "实际其他费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "实际其他费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualOtherCost;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 对账状态
        */
        @NotBlank(message = "对账状态不能为空")
        @Size(max = 32,message = "对账状态最大长度不能超过32位")
        private String status;

        /**
        * 确认时间
        */
        private LocalDate confirmDate;

        /**
        * 确认人id
        */
        @NotBlank(message = "确认人id不能为空")
        @Size(max = 19,message = "确认人id最大长度不能超过19位")
        private String confirmUserId;

        /**
        * 确认人名称
        */
        @NotBlank(message = "确认人名称不能为空")
        @Size(max = 32,message = "确认人名称最大长度不能超过32位")
        private String confirmUserName;


    }


}