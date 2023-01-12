package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SaleDetailVO {
    /**
     * 名称
     */
    private String name;

    private String productName;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销售额
     */
    private BigDecimal saleAmount;

    /**
     * 销售占比
     */
    private BigDecimal saleProportion;

    /**
     * 退货金额
     */
    private BigDecimal returnOrderAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款订单数
     */
    private Integer refundOrderQty;

    /**
     * 退货环比
     */
    private BigDecimal returnOrderRingRatio;

    /**
     * 去年销售额
     */
    private BigDecimal lastYearSaleAmount;

    /**
     * 去年销售额占比
     */
    private BigDecimal lastYearSaleProportion;

    /**
     * 前年销售额
     */
    private BigDecimal yearBeforeLastSaleAmount;

    /**
     * 前年销售额占比
     */
    private BigDecimal yearBeforeLastSaleProportion;
}