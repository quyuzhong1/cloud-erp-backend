package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname ShopNewAndOldSales
 * @Description TODO
 * @Date 2022-12-26 11:23
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ShopNewAndOldSalesVO implements Serializable {


    /**
     * 店铺名称
     */
    private String  shopName;

    /**
     * 新品销售额
     */
    private BigDecimal newSales= BigDecimal.ZERO;

    /**
     * 新品销售量
     */
    private Integer newSalesQuantity=0;


    /**
     * 老品销售额
     */
    private BigDecimal oldSales=BigDecimal.ZERO;

    /**
     * 老品销售量
     */
    private Integer oldSalesQuantity=0;
}
