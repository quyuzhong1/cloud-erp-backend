package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    private Integer salesRatio;


    /**
     * 销售同比
     */
    private Integer yearBasisRatio;



    /**
     * 销售环比
     */
    private Integer chainRelativeRatio;
}
