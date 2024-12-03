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
 * 中转费用分摊明细
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_cost_allocation_detail")
public class TransferDeclareCostAllocationDetailEntity extends BaseEntity<TransferDeclareCostAllocationDetailEntity> {

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
    * 费用类型
    */
    @TableField("fee_type")
    private String feeType;
    /**
    * 费用分摊类型
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
    * 费用分摊方式
    */
    @TableField("product_allocated_amount")
    private BigDecimal productAllocatedAmount;
    /**
    * 重量分摊方式
    */
    @TableField("weight_allocation_type")
    private String weightAllocationType;


    public static final String MAIN_ID = "main_id";

    public static final String BILL_AMOUNT = "bill_amount";

    public static final String FEE_TYPE = "fee_type";

    public static final String FEE_ALLOCATION_TYPE = "fee_allocation_type";

    public static final String ALLOCATED_AMOUNT = "allocated_amount";

    public static final String ALLOCATED_CURRENCY = "allocated_currency";

    public static final String PRODUCT_ALLOCATED_AMOUNT = "product_allocated_amount";

    public static final String WEIGHT_ALLOCATION_TYPE = "weight_allocation_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}