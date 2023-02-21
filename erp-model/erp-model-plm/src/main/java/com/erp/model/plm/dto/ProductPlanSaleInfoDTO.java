package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 13:17
 */
@Data
@NoArgsConstructor
public class ProductPlanSaleInfoDTO implements Serializable {

    /**
     * 产品规划ID
     */
    private String productPlanId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 预估销量
     */
    private Long salesQty;

    /**
     * 预估销售额
     */
    private BigDecimal salesAmount;

    /**
     * 实际销量
     */
    private Long actualSalesQty;

    /**
     * 实际销售额
     */
    private BigDecimal actualSalesAmount;

    /**
     * 销量达成率
     */
    private String salesQtyRatio;

    /**
     * 销售额达成率
     */
    private String salesAmountRatio;
}
