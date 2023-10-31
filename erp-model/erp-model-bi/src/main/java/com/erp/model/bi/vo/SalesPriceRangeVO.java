package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *  销售单价分布区间
 * @Classname

 * @Date 2022-12-19 12:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesPriceRangeVO {


    /**
     * 记录id
     */
    private String id;

    /**
     * 部门id
     */
    private String deptId;


    /**
     * 1国内 2国外标识
     */
    private Integer rangeType;


    /**
     * 开始值
     */
    private Integer startValue;

    /**
     * 结束值
     */
    private Integer endValue;

    /**
     * 销售单价
     */
    private BigDecimal sellPrice;

    /**
     * 销售额
     */
    private BigDecimal saleAmount;

    /**
     * 销售额占比
     */
    private String saleAmountRate;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销量占比
     */
    private String salesQuantityRate;

    public SalesPriceRangeVO(String id, String deptId,int rangeType, int startValue, int endValue) {
        this.id = id;
        this.deptId = deptId;
        this.rangeType = rangeType;
        this.startValue = startValue;
        this.endValue = endValue;
    }
}
