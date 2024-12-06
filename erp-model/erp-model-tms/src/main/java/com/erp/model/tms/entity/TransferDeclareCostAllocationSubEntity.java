package com.erp.model.tms.entity;

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
 * 中转费用分摊子表
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_cost_allocation_sub")
public class TransferDeclareCostAllocationSubEntity extends BaseEntity<TransferDeclareCostAllocationSubEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 报关对账明细id
    */
    @TableField("declare_reconciliation_detail_id")
    private String declareReconciliationDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String DECLARE_RECONCILIATION_DETAIL_ID = "declare_reconciliation_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}