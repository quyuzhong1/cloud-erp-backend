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
 * 小包费用分摊主表
 * </p>
 *
 * @author shukai
 * @since 2024-12-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("small_bag_cost_allocation_main")
public class SmallBagCostAllocationMainEntity extends BaseEntity<SmallBagCostAllocationMainEntity> {

    /**
    * 小包费用id
    */
    @TableField("cost_id")
    private String costId;
    /**
    * 核算期间
    */
    @TableField("report_date")
    private String reportDate;
    /**
    * 会计
    */
    @TableField("account_date")
    private String accountDate;
    /**
    * 核算状态：toBeConfirm=待确认，confirmed=已确认  枚举：SmallBagCostAllocationMainReportStatusEnum
    */
    @TableField("report_status")
    private String reportStatus;
    /**
    * 大表状态：toDo=待生成，done=已生成  枚举：SmallBagCostAllocationMainBigTableStatusEnum
    */
    @TableField("big_table_status")
    private String bigTableStatus;
    /**
    * 费用来源：confirmed=实际账单，estimateConfirm=暂估账单  枚举：SmallBagCostAllocationMainFeeSourceEnum
    */
    @TableField("fee_source")
    private String feeSource;


    public static final String COST_ID = "cost_id";

    public static final String REPORT_DATE = "report_date";

    public static final String ACCOUNT_DATE = "account_date";

    public static final String REPORT_STATUS = "report_status";

    public static final String BIG_TABLE_STATUS = "big_table_status";

    public static final String FEE_SOURCE = "fee_source";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
