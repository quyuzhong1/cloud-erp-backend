package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SkuYearSaleAmountVO {
    /**
     * 名称
     */
    private String name;

    /**
     * 金额
     */
    private BigDecimal amount;
}
