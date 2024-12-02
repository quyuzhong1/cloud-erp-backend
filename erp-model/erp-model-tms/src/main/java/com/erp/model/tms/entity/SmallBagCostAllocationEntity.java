package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 小包费用分摊
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("small_bag_cost_allocation")
public class SmallBagCostAllocationEntity extends BaseEntity<SmallBagCostAllocationEntity> {

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
    * 核算状态：toBeConfirm=待确认，confirmed=已确认  枚举：SmallBagCostAllocationReportStatusEnum
    */
    @TableField("report_status")
    private String reportStatus;
    /**
    * 大表状态：toDo=待生成，done=已生成  枚举：SmallBagCostAllocationBigTableStatusEnum
    */
    @TableField("big_table_status")
    private String bigTableStatus;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 销售出库单明细id
    */
    @TableField("outstock_detail_id")
    private String outstockDetailId;
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


    public static final String COST_ID = "cost_id";

    public static final String REPORT_DATE = "report_date";

    public static final String ACCOUNT_DATE = "account_date";

    public static final String REPORT_STATUS = "report_status";

    public static final String BIG_TABLE_STATUS = "big_table_status";

    public static final String SKU_NO = "sku_no";

    public static final String OUTSTOCK_DETAIL_ID = "outstock_detail_id";

    public static final String DELIVERY_QTY = "delivery_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
