package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0

 * @date 2023/2/20 18:56
 */
@TableName(value ="product_plan_purchase")
@Data
@NoArgsConstructor
public class ProductPlanPurchaseEntity extends BaseEntity<ProductPlanPurchaseEntity> {

    /**
     * 产品规划ID
     */
    @TableField(value = "product_plan_id")
    private String productPlanId;

    /**
     * 目标成本
     */
    @TableField(value = "target_cost")
    private BigDecimal targetCost;

    /**
     * 摸具成本预估
     */
    @TableField(value = "estimated_mold_cost")
    private BigDecimal estimatedMoldCost;

    /**
     * 供应商状态
     */
    @TableField(value = "supplier_status")
    private String supplierStatus;

    /**
     * 主要供应商名称
     */
    @TableField(value = "main_supplier_name")
    private String mainSupplierName;

}
