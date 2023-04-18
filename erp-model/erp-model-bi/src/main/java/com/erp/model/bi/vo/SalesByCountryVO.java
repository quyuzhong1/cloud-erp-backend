package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 国家销售额
 *
 * @Classname
 * @Description TODO
 * @Date 2022-12-20 8:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesByCountryVO implements Serializable {

    /**
     * sku
     */
    private String sku;

    private String productName="";

    /**
     * 国家
     */
    private String country;


    /**
     * 订单id
     */
    private String orderId;

    /**
     * 销售额
     */
    private BigDecimal sales=BigDecimal.ZERO;
}
