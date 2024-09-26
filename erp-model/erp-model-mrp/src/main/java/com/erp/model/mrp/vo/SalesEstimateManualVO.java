package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@Getter
@Setter
@NoArgsConstructor
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

    public SalesEstimateManualVO(BigDecimal currentMonthSalesQty, BigDecimal nextMonthSales, BigDecimal followingMonthSales) {
        this.currentMonthSalesQty = currentMonthSalesQty;
        this.nextMonthSales = nextMonthSales;
        this.followingMonthSales = followingMonthSales;
        LocalDate now = LocalDate.now();
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        this.currentMonthSurplusSalesQty = currentMonthSalesQty.multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(now, lastDayOfMonth)))
                .divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(now.withDayOfMonth(1), lastDayOfMonth)), 2 , RoundingMode.HALF_UP);
    }
}
