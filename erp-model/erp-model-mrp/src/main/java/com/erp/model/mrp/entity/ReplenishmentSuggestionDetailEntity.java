package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 补货建议详细
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("replenishment_suggestion_detail")
public class ReplenishmentSuggestionDetailEntity extends BaseEntity<ReplenishmentSuggestionDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * sku类型 新品/常规品
     */
    @TableField("sku_type")
    private String skuType;

    /**
     * fba可用
     */
    @TableField("fba_usable_qty")
    private Integer fbaUsableQty;

    /**
     * fba在途
     */
    @TableField("fba_in_transit_qty")
    private Integer fbaInTransitQty;

    /**
     * FBA 预计发货数量
     */
    @TableField("fba_plan_delivery_qty")
    private Integer fbaPlanDeliveryQty;

    /**
     * 海外仓可用
     */
    @TableField("overseas_usable_qty")
    private Integer overseasUsableQty;

    /**
     * 海外仓在途
     */
    @TableField("overseas_in_transit_qty")
    private Integer overseasInTransitQty;

    /**
     * 海外仓预计发货
     */
    @TableField("overseas_plan_delivery_qty")
    private Integer overseasPlanDeliveryQty;

    /**
     * 本地仓可用
     */
    @TableField("local_usable_qty")
    private Integer localUsableQty;

    /**
     * 本地仓在途
     */
    @TableField("local_in_transit_qty")
    private Integer localInTransitQty;

    /**
     * 本地仓预计采购
     */
    @TableField("local_plan_purchase_qty")
    private Integer localPlanPurchaseQty;

    /**
     * 总库存
     */
    @TableField("total_inventory_qty")
    private Integer totalInventoryQty;

    /**
     * 分时段销量  json
     */
    @TableField(jdbcType = JdbcType.OTHER)
    private JSONArray salesQty;

    /**
     * 分时段日均销  json
     */
    @TableField(jdbcType = JdbcType.OTHER)
    private JSONArray avgSalesQty;

    /**
     * 预估销量 json
     */
    @TableField(jdbcType = JdbcType.OTHER)
    private JSONArray salesEstimateQty;

    /**
     * 预估日销量  json
     */
    @TableField(jdbcType = JdbcType.OTHER)
    private JSONArray avgSalesEstimateQty;

    /**
     * 采购审批天数（天）
     */
    @TableField("purchase_approve_days")
    private Integer purchaseApproveDays;

    /**
     * 生产周期天数（天）
     */
    @TableField("production_days")
    private Integer productionDays;

    /**
     * 供应商发货天数（天）
     */
    @TableField("supplier_delivery_days")
    private Integer supplierDeliveryDays;

    /**
     * 质检入库天数（天）
     */
    @TableField("qc_days")
    private Integer qcDays;

    /**
     * 采购频率天数（天）
     */
    @TableField("purchase_cycle_days")
    private Integer purchaseCycleDays;
    /**
     * 最短发货时效（天）
     */
    @TableField("delivery_min_days")
    private Integer deliveryMinDays;
    /**
     * 默认发货时效（天）
     */
    @TableField("delivery_default_days")
    private Integer deliveryDefaultDays;
    /**
     * 最长发货时效（天）
     */
    @TableField("delivery_max_days")
    private Integer deliveryMaxDays;

    /**
     * 发货频率
     */
    @TableField("logistics_cycle_days")
    private Integer logisticsCycleDays;

    /**
     * 最短备货时效（天）
     */
    @TableField("stock_up_min_days")
    private Integer stockUpMinDays;
    /**
     * 默认备货时效（天）
     */
    @TableField("stock_up_default_days")
    private Integer stockUpDefaultDays;
    /**
     * 最长备货时效（天）
     */
    @TableField("stock_up_max_days")
    private Integer stockUpMaxDays;

    /**
     * 安全天数（天）
     */
    @TableField("safe_days")
    private Integer safeDays;

    /**
     * 入库天数（天）
     */
    @TableField("instock_days")
    private Integer instockDays;

    /**
     * fba可售天数
     */
    @TableField("fba_sellable_days")
    private Integer fbaSellableDays;

    /**
     * 可售天数
     */
    @TableField("sellable_days")
    private Integer sellableDays;

    /**
     * 海外仓可售天数
     */
    @TableField("overseas_sellable_days")
    private Integer overseasSellableDays;

    /**
     * 本地可售天数
     */
    @TableField("local_sellable_days")
    private Integer localSellableDays;

    /**
     * 总库存可售天数
     */
    @TableField("total_sellable_days")
    private Integer totalSellableDays;

    /**
     * fba在途配置
     */
    @TableField("cfg_fba_in_transit")
    private String cfgFbaInTransit;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;

    /**
     * 计算日期
     */
    @TableField("calc_date")
    private String calcDate;
    /**
     * 本条数据对应使用的规则
     */
    @TableField("cfg_rule")
    private String cfgRule;

    /**
     * 采购单价
     */
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    /**
     * 销售价
     */
    @TableField("sales_price")
    private BigDecimal salesPrice;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_TYPE = "sku_type";

    public static final String FBA_USABLE_QTY = "fba_usable_qty";

    public static final String FBA_IN_TRANSIT_QTY = "fba_in_transit_qty";

    public static final String FBA_PLAN_DELIVERY_QTY = "fba_plan_delivery_qty";

    public static final String OVERSEAS_USABLE_QTY = "overseas_usable_qty";

    public static final String OVERSEAS_IN_TRANSIT_QTY = "overseas_in_transit_qty";

    public static final String OVERSEAS_PLAN_DELIVERY_QTY = "overseas_plan_delivery_qty";

    public static final String LOCAL_USABLE_QTY = "local_usable_qty";

    public static final String LOCAL_IN_TRANSIT_QTY = "local_in_transit_qty";

    public static final String LOCAL_PLAN_PURCHASE_QTY = "local_plan_purchase_qty";

    public static final String TOTAL_INVENTORY_QTY = "total_inventory_qty";

    public static final String SALES_QTY = "sales_qty";

    public static final String AVG_SALES_QTY = "avg_sales_qty";

    public static final String SALES_ESTIMATE_QTY = "sales_estimate_qty";

    public static final String AVG_SALES_ESTIMATE_QTY = "avg_sales_estimate_qty";

    public static final String PURCHASE_APPROVE_DAYS = "purchase_approve_days";

    public static final String PRODUCTION_DAYS = "production_days";

    public static final String SUPPLIER_DELIVERY_DAYS = "supplier_delivery_days";

    public static final String QC_DAYS = "qc_days";

    public static final String PURCHASE_CYCLE_DAYS = "purchase_cycle_days";

    public static final String SAFE_DAYS = "safe_days";

    public static final String INSTOCK_DAYS = "instock_days";

    public static final String FBA_SELLABLE_DAYS = "fba_sellable_days";

    public static final String SELLABLE_DAYS = "sellable_days";

    public static final String OVERSEAS_SELLABLE_DAYS = "overseas_sellable_days";

    public static final String LOCAL_SELLABLE_DAYS = "local_sellable_days";

    public static final String TOTAL_SELLABLE_DAYS = "total_sellable_days";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
