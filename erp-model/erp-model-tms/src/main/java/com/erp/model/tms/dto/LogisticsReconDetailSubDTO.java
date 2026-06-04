package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物流商对账费用项明细 请求响应实体
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Data
@NoArgsConstructor
public class LogisticsReconDetailSubDTO implements Serializable {

    /**
     * 费用项列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 对账单 id
         */
        private String mainId;
        /**
         * 所属对账明细 id
         */
        private String detailId;
        /**
         * 同 detail 下费用项顺序
         */
        private Integer seqNo;
        /**
         * 导入识别费用名称
         */
        private String costName;
        /**
         * 映射后费用项配置 id
         */
        private String cfgCostId;
        /**
         * 映射后费用名称
         */
        private String cfgCostName;
        /**
         * 实际费用金额（原币）
         */
        private BigDecimal actualAmount;
        /**
         * 预估费用金额（原币）
         */
        private BigDecimal estimatedAmount;
        /**
         * 原币别
         */
        private String currency;
        /**
         * 结算币别
         */
        private String settlementCurrency;
        /**
         * 结算汇率（原币 → 结算币）
         */
        private BigDecimal settlementExchangeRate;
        /**
         * 本位币币别
         */
        private String localCurrency;
        /**
         * 本位币汇率（原币 → 本位币）
         */
        private BigDecimal localExchangeRate;
        /**
         * 费用项匹配状态 unmatched / matching / matched / failed
         */
        private String matchStatus;
        /**
         * 费用项匹配状态名称
         */
        private String matchStatusName;
        /**
         * 匹配失败原因
         */
        private String matchFailReason;
        /**
         * 确认状态汇总 toBeConfirm / partialConfirm / confirmed
         */
        private String reconciliationStatus;
        /**
         * 确认状态名称
         */
        private String reconciliationStatusName;
    }

    /**
     * 按 detail_id 批量查询参数
     */
    @Data
    @NoArgsConstructor
    public static class ListByDetailIdsDTO {
        /**
         * 对账明细 id 集合
         */
        @NotEmpty(message = "对账明细id集合不能为空")
        private List<String> detailIds;
    }
}
