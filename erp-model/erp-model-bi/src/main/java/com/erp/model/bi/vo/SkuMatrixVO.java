package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SkuMatrixVO {
    /**
     * 名称
     */
    private String name;

    /**
     * 销售额
     */
    private BigDecimal sales;

    /**
     * 退款+退货金额
     */
    private BigDecimal amount;
}
