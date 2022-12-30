package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 16:36
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringDTO {


    private String id;

    /**
     * 监控类型（字典bi_dict中salesMonitoringType类型，0销售额，1销量，2新品销售额，3老品销售额，4品牌销售监控，5品类销售监控，6人员销售监控）
     */
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

}
