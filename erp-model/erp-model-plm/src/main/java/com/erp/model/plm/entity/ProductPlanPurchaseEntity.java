package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 18:56
 */
@TableName(value ="product_plan_purchase")
@Data
@NoArgsConstructor
public class ProductPlanPurchaseEntity {

    /**
     * 产品规划ID
     */
    @TableField(value = "product_plan_id")
    private String productPlanId;

    /**
     * 目标成本
     */
    @TableField(value = "target_cost")
    private String targetCost;

    /**
     * 摸具成本预估
     */
    @TableField(value = "estimated_mold_cost")
    private String estimatedMoldCost;

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
