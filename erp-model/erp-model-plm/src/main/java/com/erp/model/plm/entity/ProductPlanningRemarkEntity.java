package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:32
 */
@TableName(value ="product_planning_remark")
@Data
@NoArgsConstructor
public class ProductPlanningRemarkEntity {


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
