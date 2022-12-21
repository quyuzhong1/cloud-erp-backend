package com.erp.model.bi.vo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 销量返回实体
 *
 * @Author Cloud
 * @Date 2022/12/20 17:50
 **/
@Data
@NoArgsConstructor
public class QuarterMonthSalesVO {

    /**
     * 维度
     */
    private String dimension;


    /**
     * 目标销量
     */
    private BigDecimal targetAmount;

    /**
     * 销量
     */
    private BigDecimal realAmount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    public QuarterMonthSalesVO(Map<Integer, BigDecimal> quarterTargetMap, Map<Integer, BigDecimal> quarterMap, Integer year) {
        this.dimension = year.toString()+"年销售额";
        this.targetAmount = quarterTargetMap.entrySet().stream().map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, BigDecimal.ROUND_DOWN);
        this.realAmount = quarterMap.entrySet().stream().map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, BigDecimal.ROUND_DOWN);
        this.completionRate =BigDecimal.ZERO.compareTo(this.targetAmount) == 0 ?BigDecimal.ZERO : this.realAmount.divide(this.targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
    }

    public QuarterMonthSalesVO(BigDecimal targetAmount, BigDecimal realAmount, Integer year, Integer quarter) {
        this.dimension = String.format("%sQ%s销售额", year, quarter);
        this.targetAmount = null == targetAmount ? BigDecimal.ZERO : targetAmount.setScale(4, BigDecimal.ROUND_DOWN);
        this.realAmount = null == realAmount ? BigDecimal.ZERO : realAmount.setScale(4, BigDecimal.ROUND_DOWN);
        this.completionRate =BigDecimal.ZERO.compareTo(this.targetAmount) == 0 ?BigDecimal.ZERO : this.realAmount.divide(this.targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
    }
}
