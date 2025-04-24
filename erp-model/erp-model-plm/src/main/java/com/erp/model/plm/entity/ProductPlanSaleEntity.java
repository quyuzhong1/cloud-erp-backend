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

 * @date 2023/2/20 19:06
 */
@TableName(value ="product_plan_sale")
@Data
@NoArgsConstructor
public class ProductPlanSaleEntity extends BaseEntity<ProductPlanSaleEntity> {

    /**
     * 产品规划ID
     */
    @TableField(value = "product_plan_id")
    private String productPlanId;

    /**
     * 人民币定价
     */
    @TableField(value = "price_cny")
    private BigDecimal priceCny;

    /**
     * 美元定价
     */
    @TableField(value = "price_usd")
    private BigDecimal priceUsd;

    /**
     * 销售平台/渠道
     */
    @TableField(value = "sales_platform")
    private String salesPlatform;

    /**
     * 销售目标国家ID
     */
    @TableField(value = "sales_target_country_id")
    private String salesTargetCountryId;

    /**
     * 销售目标国家
     */
    @TableField(value = "sales_target_country")
    private String salesTargetCountry;

}
