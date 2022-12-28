package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname SalesVO
 * @Description TODO
 * @Date 2022-12-16 11:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesVO implements Serializable {


    /**
     * 品名
     *
     */
    private String productName;


    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     * 销量
     */
    private Integer salesQuantity;



    /**
     *订单统计
     */
    private Integer orderCount;


    /**
     * 客单假
     */
    private BigDecimal perCustomerTransaction;

    /**
     * 名字
     */
    private String name;


    /**
     * 近七日销量
     */
    private Integer lastSevenDaysSalesQuantity;

    /**
     * 近三十天日销量
     */
    private Integer lastThirtyDaysSalesQuantity;


    /**
     * 销售趋势
     */
    private List<BigDecimal> salesTrend;
}
