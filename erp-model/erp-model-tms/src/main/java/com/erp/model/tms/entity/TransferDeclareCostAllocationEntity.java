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
 * 中转费用分摊
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_cost_allocation")
public class TransferDeclareCostAllocationEntity extends BaseEntity<TransferDeclareCostAllocationEntity> {

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
    * 核算状态：toBeConfirm=待确认，confirmed=已确认  枚举：TransferDeclareCostAllocationReportStatusEnum
    */
    @TableField("report_status")
    private String reportStatus;
    /**
    * 大表状态：toDo=待生成，done=已生成  枚举：TransferDeclareCostAllocationBigTableStatusEnum
    */
    @TableField("big_table_status")
    private String bigTableStatus;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 报关对账id
    */
    @TableField("declare_reconciliation_id")
    private String declareReconciliationId;
    /**
    * 报关对账明细id
    */
    @TableField("declare_reconciliation_detail_id")
    private String declareReconciliationDetailId;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 单SKU计费重
    */
    @TableField("sku_weight")
    private BigDecimal skuWeight;


    public static final String TRANSFER_DECLARE_ID = "transfer_declare_id";

    public static final String REPORT_DATE = "report_date";

    public static final String ACCOUNT_DATE = "account_date";

    public static final String REPORT_STATUS = "report_status";

    public static final String BIG_TABLE_STATUS = "big_table_status";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DECLARE_RECONCILIATION_ID = "declare_reconciliation_id";

    public static final String DECLARE_RECONCILIATION_DETAIL_ID = "declare_reconciliation_detail_id";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String SKU_WEIGHT = "sku_weight";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
