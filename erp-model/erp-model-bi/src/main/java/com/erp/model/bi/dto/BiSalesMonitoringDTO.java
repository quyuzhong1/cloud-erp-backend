package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/29 16:36
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringDTO {


    private String id;

    /**
     * 监控维度（字典bi_dict中salesMonitoringType类型，0部门，1人员，2店铺，3品类，4SKU，5平台，6国家）
     */
    @NotNull(message = "监控维度不能为空")
    private Integer type;

    /**
     * 最新月基础值
     */
    private BigDecimal latestMonthValue;

    /**
     * 环比(百分比)
     */
    private BigDecimal relativeRatio;

    /**
     * 最新月基础值比较符（>=,<=,>,<）
     */
    private String latestMonthCompare;

    /**
     * 环比比较符（>=,<=,>,<）
     */
    private String relativeRatioCompare;

    /**
     * 监控指标 （字典bi_dict中salesMonitoringMetrics类型，销售额 salesAmount，销量 salesQty）
     */
    @NotBlank(message = "监控指标不能为空")
    private String metrics;

}
