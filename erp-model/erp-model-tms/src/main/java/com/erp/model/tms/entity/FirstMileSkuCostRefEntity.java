package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * sku成本关系记录
 * </p>
 *
 * @author zdy
 * @since 2024-08-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_sku_cost_ref")
public class FirstMileSkuCostRefEntity extends BaseEntity<FirstMileSkuCostRefEntity> {

    /**
    * sku成本明细id
    */
    @TableField("sku_cost_detail_id")
    private String skuCostDetailId;
    /**
    * 费用分摊明细id
    */
    @TableField("first_mile_sku_allocation_id")
    private String firstMileSkuAllocationId;


    public static final String SKU_COST_DETAIL_ID = "sku_cost_detail_id";

    public static final String FIRST_MILE_SKU_ALLOCATION_ID = "first_mile_sku_allocation_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}