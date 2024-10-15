package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
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
        BigDecimal saleQty = new BigDecimal(0);
        //判断是否存在存在排除数据
        boolean excluded = salesDataList.stream().anyMatch(SalesForecastCalculatorDTO::isExcluded);
        if (Boolean.TRUE.equals(excluded)) {
            //去除所有需排除的新总权重
            BigDecimal excludedTotalWeight = salesDataList.stream().filter(v -> Boolean.FALSE.equals(v.isExcluded()))
                    .map(SalesForecastCalculatorDTO::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            //总权重
            BigDecimal totalWeight = salesDataList.stream().map(SalesForecastCalculatorDTO::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            //重算权重
            for (SalesForecastCalculatorDTO calculatorDTO : salesDataList) {
                if (Boolean.FALSE.equals(calculatorDTO.isExcluded())) {
                    //销量为  日均销量*权重占比/新总权重*旧总权重
                    saleQty = saleQty.add(calculatorDTO.getAvgSales().multiply(calculatorDTO.getWeight()).multiply(totalWeight).divide(excludedTotalWeight, 2, RoundingMode.HALF_UP));
                }
            }
        }else {
            saleQty = salesDataList.stream()
                    .map(v -> v.getAvgSales().multiply(v.getWeight()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return saleQty;
    }
}
