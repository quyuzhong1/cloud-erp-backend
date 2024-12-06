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
 * 中转费用分摊主表
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_cost_allocation_main")
public class TransferDeclareCostAllocationMainEntity extends BaseEntity<TransferDeclareCostAllocationMainEntity> {

    /**
    * 中转报关id
    */
    @TableField("transfer_declare_id")
    private String transferDeclareId;
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
    * 核算状态：toBeConfirm=待确认，confirmed=已确认  枚举：TransferDeclareCostAllocationMainReportStatusEnum
    */
    @TableField("report_status")
    private String reportStatus;
    /**
    * 大表状态：toDo=待生成，done=已生成  枚举：TransferDeclareCostAllocationMainBigTableStatusEnum
    */
    @TableField("big_table_status")
    private String bigTableStatus;


    public static final String TRANSFER_DECLARE_ID = "transfer_declare_id";

    public static final String REPORT_DATE = "report_date";

    public static final String ACCOUNT_DATE = "account_date";

    public static final String REPORT_STATUS = "report_status";

    public static final String BIG_TABLE_STATUS = "big_table_status";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
