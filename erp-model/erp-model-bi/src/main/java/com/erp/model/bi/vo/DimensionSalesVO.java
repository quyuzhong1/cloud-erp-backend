package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 维度销售额分组统计数据返回
 *
 * @Author Cloud
 * @Date 2023/1/3 14:37
 **/
@Data
@NoArgsConstructor
public class DimensionSalesVO {

    /**
     * 维度
     */
    private String dimension;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;
}
