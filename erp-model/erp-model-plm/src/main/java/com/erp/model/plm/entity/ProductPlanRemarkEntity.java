package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0

 * @date 2023/2/20 19:32
 */
@TableName(value ="product_plan_remark")
@Data
@NoArgsConstructor
public class ProductPlanRemarkEntity extends BaseEntity<ProductPlanRemarkEntity>  {


    /**
     * 产品规划ID
     */
    @TableField(value = "product_plan_id")
    private String productPlanId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}
