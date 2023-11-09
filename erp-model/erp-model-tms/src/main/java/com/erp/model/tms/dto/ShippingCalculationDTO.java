package com.erp.model.tms.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算DTO
 * @date 2023/11/9 17:47
 */
@Data
public class ShippingCalculationDTO {

    /**
     * 运费
     */
    private BigDecimal shippingCost;

    /**
     * 操作费
     */
    private BigDecimal operatingCost;

    /**
     * 挂号费
     */
    private BigDecimal registrationCost;

    /**
     * 折扣费
     */
    private BigDecimal discountCost;

    /**
     * 签名费
     */
    private BigDecimal signatureCost;

    /**
     * 保险费
     */
    private BigDecimal premiumCost;

    /**
     * 超尺寸附加费
     */
    private BigDecimal oversizeSurchargeCost;

    /**
     * 燃油附加费
     */
    private BigDecimal fuelSurchargeCost;

    /**
     * 最终运费
     */
    private BigDecimal totalShippingCost;
}
