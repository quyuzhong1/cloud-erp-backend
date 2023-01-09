package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MatrixVO {
    /**
     * 名称
     */
    private String name;

    /**
     * 销售额
     */
    private BigDecimal sales;

    /**
     * 净利润
     */
    private BigDecimal netProfit;
}
