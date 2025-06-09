package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SalesAnalysisVO {

    /**
     * 预测销量
     */
    private List<BigDecimal> estimatesSales;
    /**
     * 去噪销量
     */
    private List<BigDecimal> denoisingSales;
    /**
     * 历史销量
     */
    private List<BigDecimal> historySales;

    /**
     * 日期
     */
    private List<LocalDate> date;
}
