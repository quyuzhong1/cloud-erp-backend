package com.erp.model.mrp.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesEstimateManualVO {
    /**
     * 当月销量预估
     */
    private Integer currentMonthSalesQty;

    /**
     * 当月销量剩余预估
     */
    private Integer currentMonthSurplusSalesQty;

    /**
     * 下月销量预估
     */
    private String nextMonthSales;

    /**
     * 后月销量预估
     */
    private String followingMonthSales;
}
