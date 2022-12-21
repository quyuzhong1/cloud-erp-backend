package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *  店铺销售额
 * @Classname
 * @Description TODO
 * @Date 2022-12-21 10:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ShopSalesVO  implements Serializable {


    /**
     * 平台名称
     */
    private String  platformName;


    /**
     * 店铺名称
     */
    private String  shopName;


    /**
     * 店铺编号
     */
    private String  shopNo;

    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     * 销量
     */
    private Integer salesQuantity;


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
