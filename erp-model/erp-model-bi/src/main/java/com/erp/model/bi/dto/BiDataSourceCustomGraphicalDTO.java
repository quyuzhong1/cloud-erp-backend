package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/28 10:37
 */
@Data
@NoArgsConstructor
public class BiDataSourceCustomGraphicalDTO {

    /**
     * 指标名称
     */
    private String targetName;

    /**
     * 目标值
     */
    private BigDecimal targetValue;

    /**
     * 各个时间点对应的值
     */
    private List<BigDecimal> values;
}
