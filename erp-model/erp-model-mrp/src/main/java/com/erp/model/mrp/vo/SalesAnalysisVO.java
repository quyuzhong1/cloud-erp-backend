package com.erp.model.mrp.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private SalesVO estimatesSales;
    /**
     * 去噪销量
     */
    private SalesVO denoisingSales;
    /**
     * 历史销量
     */
    private SalesVO historySales;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesVO {
        /**
         * 日期
         */
        private List<LocalDate> date;
        /**
         * 数量
         */
        private List<BigDecimal> qty;
    }
}
