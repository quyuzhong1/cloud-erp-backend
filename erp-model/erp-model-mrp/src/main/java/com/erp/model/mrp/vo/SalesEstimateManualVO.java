package com.erp.model.mrp.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesEstimateManualVO {
    /**
     * 当月销量预估
     */
    private BigDecimal currentMonthSalesQty;

    /**
     * 当月销量剩余预估
     */
    private BigDecimal currentMonthSurplusSalesQty;

    /**
     * 下月销量预估
     */
    private BigDecimal nextMonthSales;

    /**
     * 后月销量预估
     */
    private BigDecimal followingMonthSales;
}
