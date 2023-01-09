package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/3 17:45
 */
@Data
@NoArgsConstructor
public class BiSalesMonitoringTableDTO {

    /**
     * SKU
     */
    private String skuNo;

    /**
     * 品名
     */
    private String itemName;

    /**
     * 品牌id
     */
    private String brandId;

    /**
     * 品牌
     */
    private String brandName;

    /**
     * 品类id
     */
    private String categoryId;

    /**
     * 品类
     */
    private String category;

    /**
     * 负责人id
     */
    private String chargeId;

    /**
     * 负责人
     */
    private String chargeName;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品售价
     */
    private BigDecimal sellPrice;

    /**
     * 新品标识 1为新品 0 为非新品
     */
    private Integer newSign;

    /**
     * 订单日期
     */
    private LocalDate platformCreateTime;
}
