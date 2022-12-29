package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SkuDateSaleTrendVO {
    /**
     * 时间
     */
    public String date;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销售额
     */
    private BigDecimal sales;
}
