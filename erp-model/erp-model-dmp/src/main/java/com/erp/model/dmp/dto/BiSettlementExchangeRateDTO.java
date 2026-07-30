package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 汇率相关 DTO。
 */
@NoArgsConstructor
@Data
public class BiSettlementExchangeRateDTO {

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO {
        /**
         * 币别集合。
         */
        private List<String> currencyList;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 原币别
         */
        private String sourceCurrencyCode;
        /**
         * 原币别名称
         */
        private String sourceCurrencyName;
        /**
         * 目标币别
         */
        private String targetCurrencyCode;
        /**
         * 目标币别名称
         */
        private String targetCurrencyName;
        /**
         * 直接汇率
         */
        private BigDecimal exchangeRate;
        /**
         * 间接汇率
         */
        private BigDecimal indirectExchangeRate;
        /**
         * 生效日期
         */
        private LocalDate settlementDateBegin;
        /**
         * 失效日期
         */
        private LocalDate settlementDateEnd;
        /**
         * 是否禁用，true是，false否
         */
        private Boolean disabled;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 生效日期
         */
        @NotNull(message = "生效日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate settlementDateBegin;

        /**
         * 失效日期
         */
        @NotNull(message = "失效日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate settlementDateEnd;

        /**
         * 汇率
         */
        @NotNull(message = "汇率不能为空")
        private BigDecimal exchangeRate;

        /**
         * 源币种
         */
        @NotNull(message = "源币种不能为空")
        private String sourceCurrencyCode;

        /**
         * 目标币种
         */
        @NotNull(message = "目标币种不能为空")
        private String targetCurrencyCode;

        /**
         * 禁用状态
         */
        @NotNull(message = "禁用状态不能为空")
        private Boolean disabled;

        /**
         * 汇率类型
         */
        @NotBlank(message = "汇率类型不能为空")
        private String type;

        /**
         * 间接汇率
         */
        @NotNull(message = "间接汇率不能为空")
        private BigDecimal indirectExchangeRate;

        /**
         * 金蝶id
         */
        @NotBlank(message = "金蝶id不能为空")
        private String kingdeeId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 主键id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CurrencyParamDTO {
        /**
         * 日期
         */
        private String date;
        /**
         * 币别
         */
        @NotBlank(message = "币别不能为空")
        private String currency;
    }

    /**
     * 批量汇率查询入参。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchRateParamDTO {
        /**
         * 查询日期，格式为 yyyy-MM-dd。
         */
        @NotBlank(message = "查询日期不能为空")
        private String date;

        /**
         * 源币别编码。
         */
        @NotBlank(message = "源币别编码不能为空")
        private String sourceCurrencyCode;
    }

    /**
     * 批量汇率查询出参。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchRateResultDTO {
        /**
         * 查询日期，格式为 yyyy-MM-dd。
         */
        private String date;

        /**
         * 源币别编码。
         */
        private String sourceCurrencyCode;

        /**
         * 匹配到的汇率，未命中有效汇率时为空。
         */
        private BigDecimal exchangeRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeParamDTO {
        /**
         * 汇率类型
         */
        @NotBlank(message = "汇率类型不能为空")
        private String type;

        /**
         * 结算开始日期
         */
        @NotNull(message = "结算开始日期不能为空")
        private LocalDate settlementDateBegin;

        /**
         * 结算结束日期
         */
        @NotNull(message = "结算结束日期不能为空")
        private LocalDate settlementDateEnd;

        /**
         * 目标币别编码
         */
        @NotBlank(message = "目标币别编码不能为空")
        private String targetCurrencyCode;
        /**
         * 源币别编码
         */
        @NotBlank(message = "源币别编码不能为空")
        private String sourceCurrencyCode;
    }
}
