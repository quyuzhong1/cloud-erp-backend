package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/29 17:28
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringTableVO {
    /**
     * 排名
     */
    private Integer seq;

    /**
     * SKU
     */
    private String skuNo;

    /**
     * 品名
     */
    private String itemName;

    /**
     * 品牌
     */
    private String brandName;

    /**
     * 品类
     */
    private String category;

    /**
     * 负责人
     */
    private String chargeName;

    /**
     * 年累计销售额（或销量）
     */
    private BigDecimal sumYearSale;

    /**
     * 上个月累计销售额（或销量）
     */
    private BigDecimal sumFirstMonthSale;

    /**
     * 下个月累计销售额（或销量）
     */
    private BigDecimal sumSecondMonthSale;

    /**
     * 环比
     */
    private String relativeRatioName;
}
