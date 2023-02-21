package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 12:36
 */
@Data
@NoArgsConstructor
public class ProductPlanPurchaseDTO implements Serializable {

    /**
     * 供应链信息id
     */
    private String id;

    /**
     * 产品规划ID
     */
    private String productPlanId;

    /**
     * 目标成本
     */
    private BigDecimal targetCost;

    /**Z
     * 摸具成本预估
     */
    private BigDecimal estimatedMoldCost;

    /**
     * 供应商状态
     */
    private String supplierStatusName;

    /**
     * 主要供应商名称
     */
    private String mainSupplierName;
}
