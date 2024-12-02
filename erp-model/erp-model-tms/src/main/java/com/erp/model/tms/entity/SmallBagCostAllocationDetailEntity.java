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
 * 小包费用分摊明细
 * </p>
 *
 * @author shukai
 * @since 2024-11-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("small_bag_cost_allocation_detail")
public class SmallBagCostAllocationDetailEntity extends BaseEntity<SmallBagCostAllocationDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 账单金额
    */
    @TableField("bill_amount")
    private BigDecimal billAmount;
    /**
    * 费用类型 AllocationFeeTypeEnum
    */
    @TableField("fee_type")
    private String feeType;
    /**
    * 费用分摊类型 CostAllocationEnum
    */
    @TableField("fee_allocation_type")
    private String feeAllocationType;
    /**
    * 分摊金额
    */
    @TableField("allocated_amount")
    private BigDecimal allocatedAmount;
    /**
    * 分摊币种
    */
    @TableField("allocated_currency")
    private String allocatedCurrency;
    /**
    * 单个产品分摊
    */
    @TableField("product_allocated_amount")
    private BigDecimal productAllocatedAmount;
    /**
    * 重量分摊方式
    */
    @TableField("weight_allocation_type")
    private String weightAllocationType;
    /**
    * 分摊组织id
    */
    @TableField("org_id")
    private String orgId;
    /**
    * 分摊组织名称
    */
    @TableField("org_name")
    private String orgName;


    public static final String MAIN_ID = "main_id";

    public static final String BILL_AMOUNT = "bill_amount";

    public static final String FEE_TYPE = "fee_type";

    public static final String FEE_ALLOCATION_TYPE = "fee_allocation_type";

    public static final String ALLOCATED_AMOUNT = "allocated_amount";

    public static final String ALLOCATED_CURRENCY = "allocated_currency";

    public static final String PRODUCT_ALLOCATED_AMOUNT = "product_allocated_amount";

    public static final String WEIGHT_ALLOCATION_TYPE = "weight_allocation_type";

    public static final String ORG_ID = "org_id";

    public static final String ORG_NAME = "org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}