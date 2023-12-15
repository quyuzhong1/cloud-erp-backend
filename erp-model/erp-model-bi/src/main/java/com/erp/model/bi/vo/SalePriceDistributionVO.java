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
     * 开始值
     */
    private Integer startValue;

    /**
     * 结束值
     */
    private Integer endValue;

    /**
     * 销售单价
     */
    private BigDecimal sellPrice;

    /**
     * 销售额
     */
    private BigDecimal saleAmount;

    /**
     * 销售额占比
     */
    private String saleAmountRate;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销量占比
     */
    private String salesQuantityRate;
}