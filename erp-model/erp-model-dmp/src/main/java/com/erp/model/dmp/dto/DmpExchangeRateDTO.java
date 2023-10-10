package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: 汇率
 * @date 2023/8/14 15:22
 */
@Data
@NoArgsConstructor
public class DmpExchangeRateDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 生效日期
     */
    private LocalDate settlementDateBegin;

    /**
     * 失效日期
     */
    private LocalDate settlementDateEnd;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 源币种
     */
    private String sourceCurrencyCode;

    /**
     * 目标币种
     */
    private String targetCurrencyCode;

    /**
     * 汇率类型
     */
    private String type;

    /**
     * 间接汇率
     */
    private BigDecimal indirectExchangeRate;

    /**
     * 金蝶id
     */
    private String sourceId;

    /**
     * 平台
     */
    private String platformSign;

    /**
     * 审核日期
     */
    private LocalDate approveDate;

    /**
     * 审核状态
     */
    private String approveStatus;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 禁用日期
     */
    private LocalDate disabledDate;
}
