package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RptOutOfStockVO {
    /**
     * 断货开始日期
     */
    private LocalDate startDate;

    /**
     * 断货结束日期
     */
    private LocalDate endDate;

    /**
     * 连续断货天数
     */
    private Integer days;
    /**
     * 销量
     */
    private BigDecimal salesQty;

    /**
     * 金额
     */
    private BigDecimal amount;

}
