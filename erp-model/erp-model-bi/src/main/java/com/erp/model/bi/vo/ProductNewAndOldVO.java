package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname PlatformNewAndOldVO
 * @Description TODO
 * @Date 2022-12-27 15:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductNewAndOldVO implements Serializable {

    /**
     * 平台名称
     */
    private String name;

    /**
     * 新品销售额
     */
    private BigDecimal newProductSales= BigDecimal.ZERO;

    /**
     * 新品销售量
     */
    private Integer newSalesQuantity=0;


    /**
     * 老品销售额
     */
    private BigDecimal oldProductSales=BigDecimal.ZERO;

    /**
     * 老品销售量
     */
    private Integer oldSalesQuantity=0;

    /**
     * 新品环比真正率
     */
    private Integer newProductChainRelativeRatio=0;

    /**
     * 老品环比真正率
     */
    private Integer oldProductChainRelativeRatio=0;


}
