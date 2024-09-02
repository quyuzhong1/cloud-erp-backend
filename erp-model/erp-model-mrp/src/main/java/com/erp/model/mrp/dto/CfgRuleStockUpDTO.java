package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 备货（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleStockUpDTO implements Serializable {




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
        * 采购审批天数（天）
        */
        private Integer purchaseApproveDays;

        /**
        * 生产周期天数（天）
        */
        private Integer productionDays;

        /**
        * 供应商发货天数（天）
        */
        private Integer supplierDeliveryDays;

        /**
        * 质检入库天数（天）
        */
        private Integer qcDays;

        /**
        * 采购频率天数（天）
        */
        private Integer purchaseCycleDays;

        /**
        * 安全天数（天）
        */
        private Integer safeDays;

        /**
        * 入库天数（天）
        */
        private Integer instockDays;

        /**
        * 常规品备货系数
        */
        private BigDecimal stockingRatio;

        /**
        * 新品备货系数
        */
        private BigDecimal newStockingRatio;

        /**
        * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
        */
        private String platformType;

        /**
        * 关联id
        */
        private String refId;

        /**
        * 关联类型
        */
        private String refType;

        /**
         * 物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> cfgLogisticsList;

        /**
         * 常规品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        private List<CfgRuleStockingRatioDTO.ViewDTO> newStockingRatioList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class CustomUpdateDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = true;

        /**
         * 采购审批天数（天）
         */
        private Integer purchaseApproveDays;

        /**
         * 生产周期天数（天）
         */
        private Integer productionDays;

        /**
         * 供应商发货天数（天）
         */
        private Integer supplierDeliveryDays;

        /**
         * 质检入库天数（天）
         */
        private Integer qcDays;

        /**
         * 采购频率天数（天）
         */
        private Integer purchaseCycleDays;

        /**
         * 安全天数（天）
         */
        private Integer safeDays;

        /**
         * 常规品备货系数
         */
        @Digits(integer = 12, fraction = 4, message = "常规品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stockingRatio;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32,message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;

        /**
         * 关联id
         */
        @Size(max = 19,message = "关联id最大长度不能超过19位")
        private String refId;

        /**
         * 关联类型
         */
        @Size(max = 32,message = "关联类型最大长度不能超过32位")
        private String refType;
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
        private String id;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 物流信息
         */
        @NotEmpty(message = "物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> cfgLogisticsList;

        /**
         * 常规品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> stockingRatioList;

        /**
         * 新品备货系数信息
         */
        @Valid
        private List<CfgRuleStockingRatioDTO.UpdateDTO> newStockingRatioList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 采购审批天数（天）
        */
        @NotNull(message = "采购审批天数（天）不能为空")
        @Min(value = 0,message = "采购审批天数（天）最小值为0")
        @Max(value = 365,message = "采购审批天数（天）最大值为365")
        private Integer purchaseApproveDays;

        /**
        * 生产周期天数（天）
        */
        @NotNull(message = "生产周期天数（天）不能为空")
        @Min(value = 0,message = "生产周期天数（天）最小值为0")
        @Max(value = 365,message = "生产周期天数（天）最大值为365")
        private Integer productionDays;

        /**
        * 供应商发货天数（天）
        */
        @NotNull(message = "供应商发货天数（天）不能为空")
        @Min(value = 0,message = "供应商发货天数（天）最小值为0")
        @Max(value = 365,message = "供应商发货天数（天）最大值为365")
        private Integer supplierDeliveryDays;

        /**
        * 质检入库天数（天）
        */
        @NotNull(message = "质检入库天数（天）不能为空")
        @Min(value = 0,message = "质检入库天数（天）最小值为0")
        @Max(value = 365,message = "质检入库天数（天）最大值为365")
        private Integer qcDays;

        /**
        * 采购频率天数（天）
        */
        @NotNull(message = "采购频率天数（天）不能为空")
        @Min(value = 0,message = "采购频率天数（天）最小值为0")
        @Max(value = 365,message = "采购频率天数（天）最大值为365")
        private Integer purchaseCycleDays;

        /**
        * 安全天数（天）
        */
        @NotNull(message = "安全天数（天）不能为空")
        @Min(value = 0,message = "安全天数（天）最小值为0")
        @Max(value = 365,message = "安全天数（天）最大值为365")
        private Integer safeDays;

        /**
        * 入库天数（天）
        */
        @NotNull(message = "入库天数（天）不能为空")
        @Min(value = 0,message = "入库天数（天）最小值为0")
        @Max(value = 365,message = "入库天数（天）最大值为365")
        private Integer instockDays;

        /**
        * 常规品备货系数
        */
        @NotNull(message = "常规品备货系数不能为空")
        @Digits(integer = 12, fraction = 4, message = "常规品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stockingRatio;

        /**
        * 新品备货系数
        */
        @NotNull(message = "新品备货系数不能为空")
        @Digits(integer = 12, fraction = 4, message = "新品备货系数整数位不能超过12位，小数位不能超过4位")
        private BigDecimal newStockingRatio;

        /**
        * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
        */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32,message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;

        /**
        * 关联id
        */
        @Size(max = 19,message = "关联id最大长度不能超过19位")
        private String refId;

        /**
        * 关联类型
        */
        @Size(max = 32,message = "关联类型最大长度不能超过32位")
        private String refType;

    }


}