package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname SalesFlagVO

 * @Date 2022-12-27 16:10
 * @Created by yl
 */
@Data
public class SalesFlagVO implements Serializable {

    private Integer flag;

    private String skuNo;

    private String name;

    private Integer orderCount;

    /**
     * 销售额
     */
    private BigDecimal sales;

    /**
     * 销售同比
     */
    private BigDecimal salesBasisRatio;

    /**
     * 销售环比
     */
    private BigDecimal salesChainRelativeRatio;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销量同比
     */
    private BigDecimal salesQuantityBasisRatio;

    /**
     * 销量环比
     */
    private BigDecimal salesQuantityChainRelativeRatio;

    /**
     * 客单价
     */
    private BigDecimal salesPrice;

    /**
     * 客单价同比
     */
    private BigDecimal salesPriceBasisRatio;

    /**
     * 客单价环比
     */
    private BigDecimal salesPriceChainRelativeRatio;

    public SalesFlagVO() {
        this.flag = 0;
        this.orderCount = 0;
        this.sales = BigDecimal.ZERO;
        this.salesBasisRatio = BigDecimal.ZERO;
        this.salesChainRelativeRatio = BigDecimal.ZERO;
        this.salesQuantity = 0;
        this.salesQuantityBasisRatio = BigDecimal.ZERO;
        this.salesQuantityChainRelativeRatio = BigDecimal.ZERO;
        this.salesPrice = BigDecimal.ZERO;
        this.salesPriceBasisRatio = BigDecimal.ZERO;
        this.salesPriceChainRelativeRatio = BigDecimal.ZERO;
    }
}
