package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SkuVO 简单信息（B2C订单消费用）
 */
@Data
@NoArgsConstructor
public class SkuInfoSimpleVO implements Serializable {


    private String skuId;

    private String productId;
    /**
     * sku no
     */
    private String skuNo;


    private String skuImagesUrl;

    /**
     * 标准零售价
     */
    private BigDecimal retailPrice;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 目标含税成本
     */
    private BigDecimal targetTaxCost;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 实际含税成本
     */
    private BigDecimal actualTaxCost;


}
