package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname SalesFlagVO
 * @Description TODO
 * @Date 2022-12-27 16:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesFlagVO implements Serializable {


    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     * 销量
     */
    private Integer salesQuantity;


    private String flag;

    private String skuNo;


    private String name;




    private Integer  orderCount;
}
