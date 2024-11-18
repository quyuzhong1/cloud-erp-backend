package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 头程费用SKU分摊
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_sku_cost_allocation")
public class FirstMileSkuCostAllocationEntity extends BaseEntity<FirstMileSkuCostAllocationEntity> {

    /**
    * 主表id(first_mile_cost_allocation.id)
    */
    @TableField("main_id")
    private String mainId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * skuNO
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台skuNo
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 分摊重量 kg
    */
    @TableField("allocated_weight")
    private BigDecimal allocatedWeight;
    /**
    * 单位成本
    */
    @TableField("product_cost")
    private BigDecimal productCost;
    /**
    * 产品总成本
    */
    @TableField("product_total_cost")
    private BigDecimal productTotalCost;
    /**
    * 期初签收数量
    */
    @TableField("init_receive_qty")
    private Integer initReceiveQty;
    /**
    * 上月签收数量
    */
    @TableField("last_month_receive_qty")
    private Integer lastMonthReceiveQty;
    /**
    * 本月签收数量
    */
    @TableField("current_month_receive_qty")
    private Integer currentMonthReceiveQty;
    /**
     * 截止本月签收数量
     */
    @TableField("as_current_month_receive_qty")
    private Integer asCurrentMonthReceiveQty;
    /**
    * 截止上月签收数量
    */
    @TableField("as_last_month_receive_qty")
    private Integer asLastMonthReceiveQty;
    /**
    * 重量单位（默认kg）
    */
    @TableField("weight_unit")
    private String weightUnit;
    /**
    * 币种（默认CNY）
    */
    @TableField("currency")
    private String currency;
    /**
     * 币种符号
     */
    @TableField("currency_symbol")
    private String currencySymbol;
    /**
     * 账单来源：estimated=预估账单，actual=实际账单
     * ReconciliationBillTypeEnum
     *
    */
    @TableField("bill_source_type")
    private String billSourceType;
    /**
     * 暂估账单id
     */
    @TableField("estimated_bill_id")
    private String estimatedBillId;
    /**
     * 重量分摊id
     */
    @TableField("weight_allocation_id")
    private String weightAllocationId;

    /**
     * 发货单明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
     * 期初费用分摊明细id
     */
    @TableField("init_first_mile_detail_id")
    private String initFirstMileDetailId;
    /**
     * 对账单明细id
     */
    @TableField("reconciliation_detail_id")
    private String reconciliationDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String ALLOCATED_WEIGHT = "allocated_weight";

    public static final String PRODUCT_COST = "product_cost";

    public static final String PRODUCT_TOTAL_COST = "product_total_cost";

    public static final String INIT_RECEIVE_QTY = "init_receive_qty";

    public static final String LAST_MONTH_RECEIVE_QTY = "last_month_receive_qty";

    public static final String CURRENT_MONTH_RECEIVE_QTY = "current_month_receive_qty";

    public static final String AS_LAST_MONTH_RECEIVE_QTY = "as_last_month_receive_qty";

    public static final String WEIGHT_UNIT = "weight_unit";

    public static final String FIELD_CURRENCY = "currency";

    public static final String BILL_SOURCE_TYPE = "bill_source_type";


    public static final String ESTIMATED_BILL_ID = "estimated_bill_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}