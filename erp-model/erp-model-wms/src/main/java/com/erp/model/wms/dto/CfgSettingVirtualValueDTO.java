package com.erp.model.wms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * 虚拟仓配置
 */
@Data
@NoArgsConstructor
public class CfgSettingVirtualValueDTO implements Serializable {

    /**
     * 销售看板DTO
     */
    @Data
    @NoArgsConstructor
    public static class SalesDashboardDTO{

        /**
         * 订单类型，b2b,b2c,firstMile
         */
        @NotEmpty(message = "订单类型不能为空")
        private List<String> orderTypeList;

        /**
         * b2b销售订单状态
         */
        @Valid
        private StatusDTO b2bStatusDTO;

        /**
         * b2c销售订单状态
         */
        @Valid
        private StatusDTO b2cStatusDTO;

        /**
         * 头程订单状态
         */
        @Valid
        private StatusDTO firstMileStatusDTO;

        /**
         * 统计时长集合
         */
        @NotEmpty(message = "统计时长不能为空")
        private List<String> statDurationList;

        /**
         * 是否预警
         */
        @NotNull(message = "是否预警不能为空")
        private Boolean isWarn;

        /**
         * 预警条件
         */
        @Valid
        @NotNull(message = "预警条件不能为空")
        private WarnConditionDTO warnConditionDTO;
    }

    /**
     * 销售看板DTO
     */
    @Data
    @NoArgsConstructor
    public static class ReportOrderDemandDTO{

        /**
         * 订单类型，b2b,b2c,firstMile
         */
        @NotEmpty(message = "订单类型不能为空")
        private List<String> orderTypeList;

        /**
         * b2b销售订单状态
         */
        @Valid
        private StatusDTO b2bStatusDTO;

        /**
         * b2c销售订单状态
         */
        @Valid
        private StatusDTO b2cStatusDTO;

        /**
         * 头程订单状态
         */
        @Valid
        private StatusDTO firstMileStatusDTO;
    }

    /**
     * 状态DTO
     */
    @Data
    @NoArgsConstructor
    public static class StatusDTO{

        /**
         * 单据状态集合
         */
        private List<String> statusList;

        /**
         * 作废状态集合
         */
        @NotEmpty(message = "作废状态不能为空")
        private List<Boolean> invalidStatusList;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;
    }

    /**
     * 状态DTO
     */
    @Data
    @NoArgsConstructor
    public static class WarnConditionDTO{

        /**
         * 比较类型
         */
        @NotNull(message = "库存预警比较类型不能为空")
        private String compareType;

        /**
         * 天数类型
         */
        @NotEmpty(message = "库存预警天数不能为空")
        private List<String> daysTypeList;
    }

    /**
     * 缺货统计DTO
     */
    @Data
    @NoArgsConstructor
    public static class VirtualRuleDTO{

        /**
         * 定时更新时间
         */
        @NotEmpty(message = "定时更新时间不能为空")
        @JsonFormat(pattern = "HH:mm")
        private List<LocalTime> execTimeList;


        /**
         * 是否拆分组合品
         */
        @NotNull(message = "是否拆分组合品不能为空")
        private Boolean isSplit;
    }

    /**
     * 虚拟仓调拨DTO
     */
    @Data
    @NoArgsConstructor
    public static class VirtualTransferSettingDTO{

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 条件
         */
        private List<VirtualTransferConditionElement> conditionElementList;

    }

    @Data
    @NoArgsConstructor
    public static class InventoryAgeTO {
        /**
         * 库龄统计天数集合
         */
        @NotEmpty(message = "库龄统计天数配置不能为空")
        @Valid
        private List<InventoryAgeDateTO> list;
    }

    /**
     * 库龄统计
     */
    @Data
    @NoArgsConstructor
    public static class InventoryAgeDateTO {
        /**
         * 开始天数
         */
        @NotNull(message = "开始天数不能为空")
        private Integer startDays;
        /**
         * 结束天数
         */
        private Integer endDays;
    }

    @Data
    public static class VirtualTransferConditionElement{
        /**
         * 左括号
         */

        private String leftBracket;

        /**
         * 对应字段
         */
        @NotBlank(message = "条件字段不能为空")
        private String field;

        /**
         * 选项逻辑关系 大于 等于 等等
         */
        @NotBlank(message = "比较符号不能为空")
        private String compare;

        /**
         * 对应的值
         */
        @NotEmpty(message = "值字段不能为空")
        private List<String> valueList;

        private String value;

        /**
         * 右括号
         */
        private String rightBracket;

        /**
         * 逻辑关系 and 或者or
         */
        private String logic;

        /**
         * 对应的值类型
         */
        private String valueType;
    }
}