package com.erp.model.mrp.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SalesForecastCalculatorDTO {
    /**
     * X 天日均销量
     */
    BigDecimal avgSales;
    /**
     * X 天日均权重
     */
    BigDecimal weight;
    /**
     * 是否排除
     */
    boolean isExcluded;

    public SalesForecastCalculatorDTO(BigDecimal avgSales, BigDecimal weight, boolean isExcluded) {
        this.avgSales = avgSales;
        this.weight = weight;
        this.isExcluded = isExcluded;

    }

    public static BigDecimal calculateForecastedSales(List<SalesForecastCalculatorDTO> salesDataList) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalValidWeight = BigDecimal.ZERO;
        BigDecimal forecastedSales = BigDecimal.ZERO;

        // 计算有效权重总和，过滤断货或去噪的天数
        for (SalesForecastCalculatorDTO data : salesDataList) {
            totalWeight = totalWeight.add(data.weight);
            if (!data.isExcluded) {
                totalValidWeight = totalValidWeight.add(data.weight);
            }
        }

        // 如果所有天数都属于断货或去噪，直接返回 0
        if (totalValidWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 计算预估日销量
        for (SalesForecastCalculatorDTO data : salesDataList) {
            if (!data.isExcluded) {
                BigDecimal adjustedWeight = data.weight.divide(totalValidWeight, 6, RoundingMode.HALF_UP)
                        .multiply(totalWeight);
                forecastedSales = forecastedSales.add(data.avgSales.multiply(adjustedWeight));
            }
        }
        // 四舍五入，保留两位小数
        return forecastedSales.setScale(2, RoundingMode.HALF_UP);
    }
}
