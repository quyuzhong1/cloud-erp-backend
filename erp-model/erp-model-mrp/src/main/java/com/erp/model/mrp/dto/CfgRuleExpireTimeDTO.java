package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
public class CfgRuleExpireTimeDTO {
    @Getter
    @Setter
    public static class UpdateDTO {
        /**
         * 采购审批天数（天）
         */
        @NotNull(message = "采购审批天数（天）不能为空")
        @Min(value = 0, message = "采购审批天数（天）最小值为0")
        @Max(value = 365, message = "采购审批天数（天）最大值为365")
        private Integer purchaseApproveDays;

        /**
         * 生产周期天数（天）
         */
        @NotNull(message = "生产周期天数（天）不能为空")
        @Min(value = 0, message = "生产周期天数（天）最小值为0")
        @Max(value = 365, message = "生产周期天数（天）最大值为365")
        private Integer productionDays;

        /**
         * 供应商发货天数（天）
         */
        @NotNull(message = "供应商发货天数（天）不能为空")
        @Min(value = 0, message = "供应商发货天数（天）最小值为0")
        @Max(value = 365, message = "供应商发货天数（天）最大值为365")
        private Integer supplierDeliveryDays;

        /**
         * 质检入库天数（天）
         */
        @NotNull(message = "质检入库天数（天）不能为空")
        @Min(value = 0, message = "质检入库天数（天）最小值为0")
        @Max(value = 365, message = "质检入库天数（天）最大值为365")
        private Integer qcDays;

        /**
         * 采购频率天数（天）
         */
        @NotNull(message = "采购频率天数（天）不能为空")
        @Min(value = 0, message = "采购频率天数（天）最小值为0")
        @Max(value = 365, message = "采购频率天数（天）最大值为365")
        private Integer purchaseCycleDays;


        /**
         * 平台入库天数（天）
         */
        @NotNull(message = "平台入库天数（天）不能为空")
        @Min(value = 0, message = "平台入库天数（天）最小值为0")
        @Max(value = 365, message = "平台入库天数（天）最大值为365")
        private Integer platformInstockDays;

        /**
         * 海外仓入库天数（天）
         */
        @NotNull(message = "海外仓入库天数（天）不能为空")
        @Min(value = 0, message = "海外仓入库天数（天）最小值为0")
        @Max(value = 365, message = "海外仓入库天数（天）最大值为365")
        private Integer overseasInstockDays;

        /**
         * 海外仓入库天数明细
         */
        @Valid
        private List<CfgRuleOverseasInstockDaysDTO.UpdateDTO> overseasInstockDaysList;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 物流信息
         */
        @NotEmpty(message = "fba物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> platformCfgLogisticsList;

        /**
         * 物流信息
         */
        @NotEmpty(message = "海外仓物流信息配置不能为空")
        @Valid
        private List<CfgRuleLogisticsDTO.UpdateDTO> overseasCfgLogisticsList;


        /**
         * 关联id
         */
        @Size(max = 19, message = "关联id最大长度不能超过19位")
        private String refId;

        /**
         * 关联类型
         */
        @Size(max = 32, message = "关联类型最大长度不能超过32位")
        private String refType;

    }

    @Getter
    @Setter
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

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
         * 平台入库天数（天）
         */
        private Integer platformInstockDays;

        /**
         * 海外仓入库天数（天）
         */
        private Integer overseasInstockDays;

        /**
         * 海外仓入库天数明细
         */
        private List<CfgRuleOverseasInstockDaysDTO.ViewDTO> overseasInstockDaysList;

        /**
         * 是否是自定义
         */
        private Boolean isCustom = false;

        /**
         * 平台仓物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> platformCfgLogisticsList;

        /**
         * 海外仓物流信息
         */
        private List<CfgRuleLogisticsDTO.ViewDTO> overseasCfgLogisticsList;

    }
}
