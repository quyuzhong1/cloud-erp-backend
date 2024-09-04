package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 头程费用分摊
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_cost_allocation")
public class FirstMileCostAllocationEntity extends BaseEntity<FirstMileCostAllocationEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 实际对账单明细id
    */
    @TableField("reconciliation_id")
    private String reconciliationId;
    /**
     * 暂估对账单id
     */
    @TableField("estimated_bill_id")
    private String estimatedBillId;
    /**
    * 物流单id
    */
    @TableField("logistics_bill_id")
    private String logisticsBillId;

    /**
    * 核算期间id
    */
    @TableField("report_period_id")
    private String reportPeriodId;
    /**
     * 核算期间月份
     */
    @TableField(exist = false)
    private LocalDate reportPeriodMonth;
    /**
    * 会计期间
    */
    @TableField("account_period")
    private String accountPeriod;
    /**
    * 核算状态：waitConfirm=待确认，confirm=已确认
     * 接口地址  http://172.16.100.11:3002/project/128/interface/api/25522  key = allocationStatus
     * ConfirmStatusEnum
    */
    @TableField("status")
    private String status;
    /**
     * 发货单id
     */
    @TableField("source_id")
    private String sourceId;
    /**
     * 发货单编码
     */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 物流商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 物流商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * {业务单号}取值发货单关联的业务单号
        FBA：取值FBA货件单号
        第三方仓：海外仓入库单号
    */
    @TableField("business_code")
    private String businessCode;
    /**
    * 业务类型：demandOverseasWarehouse=第三方仓，demandPlatformWarehouse=FBA
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 对账月份
    */
    @TableField("reconciliation_month")
    private LocalDate reconciliationMonth;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 发货仓库id
    */
    @TableField("from_warehouse_id")
    private String fromWarehouseId;
    /**
    * 发货仓库名称
    */
    @TableField("from_warehouse_name")
    private String fromWarehouseName;
    /**
    * 目的仓库id
    */
    @TableField("to_warehouse_id")
    private String toWarehouseId;
    /**
    * 目的仓库名称
    */
    @TableField("to_warehouse_name")
    private String toWarehouseName;
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


    public static final String REMARK = "remark";

    public static final String RECONCILIATION_ID = "reconciliation_id";

    public static final String LOGISTICS_BILL_ID = "logistics_bill_id";

    public static final String SKU_COST_ID = "sku_cost_id";

    public static final String WEIGHT_ALLOCATION_ID = "weight_allocation_id";

    public static final String REPORT_PERIOD_ID = "report_period_id";

    public static final String ACCOUNT_PERIOD = "account_period";

    public static final String STATUS = "status";

    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String BUSINESS_CODE = "business_code";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String TRANSPORT_NO = "transport_no";

    public static final String RECONCILIATION_MONTH = "reconciliation_month";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String FROM_WAREHOUSE_ID = "from_warehouse_id";

    public static final String FROM_WAREHOUSE_NAME = "from_warehouse_name";

    public static final String TO_WAREHOUSE_ID = "to_warehouse_id";

    public static final String TO_WAREHOUSE_NAME = "to_warehouse_name";

    public static final String DELIVERY_TIME = "delivery_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}