package com.erp.model.tms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 头程费用SKU分摊明细
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_sku_cost_allocation_detail")
public class FirstMileSkuCostAllocationDetailEntity extends BaseEntity<FirstMileSkuCostAllocationDetailEntity> {

    /**
    * 主表id(first_mile_cost_allocation.id)
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku分摊主表id(first_mile_sku_cost_allocation.id)
    */
    @TableField("cost_main_id")
    private String costMainId;
    /**
    * 费用类型：shippingCost=运费，tariffFee=关税，otherTaxFee=其他税费，otherFee=其他费用
     * AllocationFeeTypeEnum
    */
    @TableField("fee_type")
    private String feeType;
    /**
    * 费用分摊方式：weightAllocation=按重量分摊，costAllocation=按成本分摊
     * CostAllocationEnum
    */
    @TableField("allocation_type")
    private String allocationType;
    /**
    * 头程总金额
    */
    @TableField("amount")
    private BigDecimal amount;
    /**
    * 头程分摊金额
    */
    @TableField("allocated_amount")
    private BigDecimal allocatedAmount;
    /**
    * 单个产品分摊金额
    */
    @TableField("product_allocated_amount")
    private BigDecimal productAllocatedAmount;
    /**
    * 期初在途费用
    */
    @TableField("init_transit_cost")
    private BigDecimal initTransitCost;
    /**
    * 期初暂估费用
    */
    @TableField("init_estimated_cost")
    private BigDecimal initEstimatedCost;
    /**
    * 冲期初在途费用
    */
    @TableField("mid_period_transit_cost")
    private BigDecimal midPeriodTransitCost;
    /**
    * 本期分摊费用
    */
    @TableField("current_period_allocated_cost")
    private BigDecimal currentPeriodAllocatedCost;
    /**
    * 期末在途费用
    */
    @TableField("end_period_transit_cost")
    private BigDecimal endPeriodTransitCost;
    /**
    * 期末暂估费用
    */
    @TableField("end_period_estimated_cost")
    private BigDecimal endPeriodEstimatedCost;
    /**
     * skuId
     */
    @TableField(exist = false)
    private String skuId;

    public static final String MAIN_ID = "main_id";

    public static final String COST_MAIN_ID = "cost_main_id";

    public static final String FEE_TYPE = "fee_type";

    public static final String ALLOCATION_TYPE = "allocation_type";

    public static final String AMOUNT = "amount";

    public static final String ALLOCATED_AMOUNT = "allocated_amount";

    public static final String PRODUCT_ALLOCATED_AMOUNT = "product_allocated_amount";

    public static final String INIT_TRANSIT_COST = "init_transit_cost";

    public static final String INIT_ESTIMATED_COST = "init_estimated_cost";

    public static final String MID_PERIOD_TRANSIT_COST = "mid_period_transit_cost";

    public static final String CURRENT_PERIOD_ALLOCATED_COST = "current_period_allocated_cost";

    public static final String END_PERIOD_TRANSIT_COST = "end_period_transit_cost";

    public static final String END_PERIOD_ESTIMATED_COST = "end_period_estimated_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}