package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Classname SalesCountVO
 * @Description TODO
 * @Date 2022-12-26 14:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesCountVO extends SalesRatioVO {


    /**
     * 名称
     */
    private String name;


    /**
     * 销量
     */
    private Integer salesQuantity;


    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     *订单量
     */
    private Integer orderCount;
}
