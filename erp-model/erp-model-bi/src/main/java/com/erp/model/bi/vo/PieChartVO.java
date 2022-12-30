package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 饼状图数据结构
 *
 * @Author Cloud
 * @Date 2022/12/30 15:05
 **/

@Data
@NoArgsConstructor
public class PieChartVO {

    /**
     * 具体数值
     */
    private BigDecimal value;

    /**
     * 名称
     */
    private String name;

    /**
     * 百分比
     */
    private BigDecimal percent;

    public PieChartVO(String key, BigDecimal value, BigDecimal totalAmount) {
        this.name = key;
        this.value = value;
        this.percent = value.divide(totalAmount, 4, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100));
    }
}
