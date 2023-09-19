package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
     * 1国内 2国外标识
     */
    private Integer type;


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
     * 销售额
     */
    private String saleAmountRate;

    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     * 销量
     */
    private String salesQuantityRate;

    public SalesPriceRangeVO(String id, int type, int startValue, int endValue) {
        this.id = id;
        this.type = type;
        this.startValue = startValue;
        this.endValue = endValue;
    }
}
