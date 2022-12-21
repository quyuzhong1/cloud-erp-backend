package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

}
