package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Classname SalePriceDistributionVO

 * @Date 2023-9-19 18:39
 * @Created by zdy
 */
@Data
@NoArgsConstructor
public class SalePriceDistributionVO {
    /**
     * 商品销售明细id
     */
    private String id;

    /**
     * 销售单价
     */
    private BigDecimal sellPrice;

    /**
     * 销售额
     */
    private BigDecimal saleAmount;

    /**
     * 销量
     */
    private Integer salesQuantity;

//    /**
//     * 退货金额
//     */
//    private BigDecimal returnOrderAmount;
//
//    /**
//     * 退款金额
//     */
//    private BigDecimal refundAmount;
//
//    /**
//     * 退款订单数
//     */
//    private Integer refundOrderQty;
//
//    /**
//     * 退货环比
//     */
//    private BigDecimal returnOrderRingRatio;
//
//    /**
//     * 去年销售额
//     */
//    private BigDecimal lastYearSaleAmount;
//
//    /**
//     * 去年销售额占比
//     */
//    private BigDecimal lastYearSaleProportion;
//
//    /**
//     * 前年销售额
//     */
//    private BigDecimal yearBeforeLastSaleAmount;
//
//    /**
//     * 前年销售额占比
//     */
//    private BigDecimal yearBeforeLastSaleProportion;
}