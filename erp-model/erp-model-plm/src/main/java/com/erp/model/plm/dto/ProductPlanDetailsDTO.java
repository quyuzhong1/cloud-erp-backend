package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 12:43
 */
@Data
@NoArgsConstructor
public class ProductPlanDetailsDTO implements Serializable {

    /**
     * 规划信息
     */
    private ProductPlanDTO productPlanDTO;

    /**
     * 销售信息
     */
    private ProductPlanSaleDTO productPlanSaleDTO;

    /**
     * 供应链信息
     */
    private ProductPlanPurchaseDTO productPlanPurchaseDTO;

    /**
     * 备注信息
     */
    private List<ProductPlanRemarkDTO> remarkList;

    /**
     * 销售数据
     */
    private List<ProductPlanSaleInfoDTO> saleInfoList;

    /**
     * 销售总量
     */
    private BigDecimal totalAmount;

    /**
     * 销售总销售额
     */
    private Integer totalQty;

    /**
     * 产品规划进度
     */
    private List<ProductPlanProgressDTO> progressList;
}
