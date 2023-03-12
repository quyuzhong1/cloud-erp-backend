package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
* @Description 产品成本信息表
* @Author Luo_WG
* @Date 2022/9/22 16:03
**/
@TableName(value ="product_cost")
@Data
public class ProductCostEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 目标含税成本
     */
    @TableField(value = "target_tax_cost", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal targetTaxCost;

    /**
     * 目标不含税成本
     */
    @TableField(value = "target_no_tax_cost", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal targetNoTaxCost;

    /**
     * 实际含税成本
     */
    @TableField(value = "actual_tax_cost", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal actualTaxCost;

    /**
     * 实际不含税成本
     */
    @TableField(value = "actual_no_tax_cost", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal actualNoTaxCost;

    /**
     * 标准零售价
     */
    @TableField(value = "retail_price")
    private BigDecimal retailPrice;

    /**
     * 目标毛利率
     */
    @TableField(value = "target_gpm")
    private BigDecimal targetGpm;

    /**
     * 实际毛利率（人民币）
     */
    @TableField(value = "actual_gpm_cny")
    private BigDecimal actualGpmCny;

    /**
     * 实际毛利率（美元）
     */
    @TableField(value = "actual_gpm_usd")
    private BigDecimal actualGpmUsd;

    /**
     * 立项成本
     */
    @TableField(value = "project_approval_cost")
    private BigDecimal projectApprovalCost;

    /**
     * 量产成本
     */
    @TableField(value = "mass_cost")
    private BigDecimal massCost;

    /**
     * 项目成本
     */
    @TableField(value = "project_cost")
    private BigDecimal projectCost;


    /**
     * 税率
     */
    @TableField(value = "tax_rate")
    private BigDecimal taxRate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}