package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *  销售基础数据
 * @Classname
 * @Description TODO
 * @Date 2022-12-19 12:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SalesBaseVO {

    /**
     * 销售额
     */
    private BigDecimal sales;


    /**
     * 销量
     */
    private Integer salesQuantity;

    /**
     *订单统计
     */
    private Integer orderCount;
    
    /**
     * 标识时间
     * 订单时间或者发货时间
     */
    private LocalDateTime flagDate;
    
}
