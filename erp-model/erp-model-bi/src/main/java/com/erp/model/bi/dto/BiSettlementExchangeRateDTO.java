package com.erp.model.bi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 9:30
 */
@NoArgsConstructor
@Data
public class BiSettlementExchangeRateDTO {

    /**
     * 主键id
     */
    private String id;

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
    private BigDecimal sourceCurrencyCode;

    /**
     * 目标币种
     */
    @NotNull(message = "目标币种不能为空")
    private BigDecimal targetCurrencyCode;
}
