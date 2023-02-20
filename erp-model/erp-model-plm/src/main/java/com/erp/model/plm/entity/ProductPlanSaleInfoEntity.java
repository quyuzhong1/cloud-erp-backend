package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:19
 */
@TableName(value ="product_plan_sale_information")
@Data
@NoArgsConstructor
public class ProductPlanSaleInfoEntity {

    /**
     * 产品规划ID
     */
    @TableField(value = "product_plan_id")
    private String productPlanId;

    /**
     * 年份
     */
    @TableField(value = "year")
    private Integer year;

    /**
     * 月份
     */
    @TableField(value = "month")
    private Integer month;

    /**
     * 销量
     */
    @TableField(value = "sales_qty")
    private Long salesQty;

    /**
     * 销售额
     */
    @TableField(value = "sales_amount")
    private BigDecimal salesAmount;

}
