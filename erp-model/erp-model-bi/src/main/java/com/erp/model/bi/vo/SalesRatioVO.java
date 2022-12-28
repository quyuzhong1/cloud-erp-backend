package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname SalesRatioVO
 * @Description TODO
 * @Date 2022-12-26 14:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesRatioVO implements Serializable {

    /**
     * 销售占比
     */
    private BigDecimal salesRatio;


    /**
     * 销售同比
     */
    private BigDecimal yearBasisRatio=BigDecimal.ZERO;



    /**
     * 销售环比
     */
    private BigDecimal chainRelativeRatio=BigDecimal.ZERO;
}
