package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 季度销售额返回实体
 *
 * @Author Cloud
 * @Date 2022/12/20 17:50
 **/
@Data
@NoArgsConstructor
public class QuarterMonthSalesVolumeVO {
    /**
     * 维度
     */
    private String dimension;


    /**
     * 目标销售额
     */
    private Integer targetNum;

    /**
     * 销售额
     */
    private Integer realNum;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    public QuarterMonthSalesVolumeVO(Map<Integer, Integer> quarterTargetMap, Map<Integer, Integer> quarterMap, Integer year) {
        this.dimension = year.toString()+"年销量";
        this.targetNum = quarterTargetMap.entrySet().stream().map(Map.Entry::getValue).reduce(0, Integer::sum);
        this.realNum = quarterMap.entrySet().stream().map(Map.Entry::getValue).reduce(0, Integer::sum);
        this.completionRate = 0 == this.targetNum ?BigDecimal.ZERO : new BigDecimal(this.realNum).divide(new BigDecimal(this.targetNum), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
    }

    public QuarterMonthSalesVolumeVO(Integer targetAmount, Integer realAmount, Integer year, Integer quarter) {
        if (null == year){
            this.dimension = String.format("%s月销量", quarter);
        }else {
            this.dimension = String.format("%sQ%s销售额", year, quarter);
        }
        this.targetNum = null == targetAmount ? 0 : targetAmount;
        this.realNum = null == realAmount ? 0 : realAmount;
        this.completionRate = 0 == this.targetNum ?BigDecimal.ZERO : new BigDecimal(this.realNum).divide(new BigDecimal(this.targetNum), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
    }

}
