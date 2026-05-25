package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * FBS库存
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("fbs_inventory")
public class FbsInventoryEntity extends BaseEntity<FbsInventoryEntity> {

    /**
     * 店铺Id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 仓库Id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 平台SKU
     */
    @TableField("platform_sku")
    private String platformSku;

    /**
     * 平台产品名称
     */
    @TableField("platform_product_name")
    private String platformProductName;

    /**
     * FBS仓SKU
     */
    @TableField("fbs_sku")
    private String fbsSku;

    /**
     * 规格名称
     */
    @TableField("spec_name")
    private String specName;

    /**
     * SKUId
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * SKU
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 履行/采购模式
     */
    @TableField("purchase_mode")
    private String purchaseMode;

    /**
     * 推荐补货数
     */
    @TableField("recommended_replenishment_qty")
    private Integer recommendedReplenishmentQty;

    /**
     * 总库存
     */
    @TableField("total_stock_qty")
    private Integer totalStockQty;

    /**
     * 已库收入库中
     */
    @TableField("stocked_inbound_qty")
    private Integer stockedInboundQty;

    /**
     * 转ASN入库中
     */
    @TableField("transfer_asn_inbound_qty")
    private Integer transferAsnInboundQty;

    /**
     * 已预留
     */
    @TableField("reserved_qty")
    private Integer reservedQty;

    /**
     * 不可销售
     */
    @TableField("unsellable_qty")
    private Integer unsellableQty;

    /**
     * 在途
     */
    @TableField("in_transit_qty")
    private Integer inTransitQty;

    /**
     * 周转天数
     */
    @TableField("turnover_days")
    private Integer turnoverDays;

    /**
     * 仓库库存能覆盖的销售天数
     */
    @TableField("warehouse_inventory_coverage_days")
    private Integer warehouseInventoryCoverageDays;

    /**
     * 销售速率（数量每天）
     */
    @TableField("daily_avg_sales_qty")
    private BigDecimal dailyAvgSalesQty;

    /**
     * 最近7天销量
     */
    @TableField("last_7_days_sales_qty")
    private Integer last7DaysSalesQty;

    /**
     * 最近15天销量
     */
    @TableField("last_15_days_sales_qty")
    private Integer last15DaysSalesQty;

    /**
     * 最近30天销量
     */
    @TableField("last_30_days_sales_qty")
    private Integer last30DaysSalesQty;

    /**
     * 最近60天销量
     */
    @TableField("last_60_days_sales_qty")
    private Integer last60DaysSalesQty;

    /**
     * 最近90天销量
     */
    @TableField("last_90_days_sales_qty")
    private Integer last90DaysSalesQty;

    /**
     * 库龄0-30天
     */
    @TableField("stock_age_0_30_qty")
    private Integer stockAge030Qty;

    /**
     * 库龄31-60天
     */
    @TableField("stock_age_31_60_qty")
    private Integer stockAge3160Qty;

    /**
     * 库龄61-90天
     */
    @TableField("stock_age_61_90_qty")
    private Integer stockAge6190Qty;

    /**
     * 库龄91-120天
     */
    @TableField("stock_age_91_120_qty")
    private Integer stockAge91120Qty;

    /**
     * 库龄121-180天
     */
    @TableField("stock_age_121_180_qty")
    private Integer stockAge121180Qty;

    /**
     * 库龄大于180天
     */
    @TableField("stock_age_over_180_qty")
    private Integer stockAgeOver180Qty;

    /**
     * 更新时间
     */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;

    public static final String SHOP_ID = "shop_id";
    public static final String SHOP_NAME = "shop_name";
    public static final String WAREHOUSE_ID = "warehouse_id";
    public static final String WAREHOUSE_NAME = "warehouse_name";
    public static final String PLATFORM_SKU = "platform_sku";
    public static final String PLATFORM_PRODUCT_NAME = "platform_product_name";
    public static final String FBS_SKU = "fbs_sku";
    public static final String SPEC_NAME = "spec_name";
    public static final String SKU_ID = "sku_id";
    public static final String SKU_NO = "sku_no";
    public static final String PRODUCT_NAME = "product_name";
    public static final String PURCHASE_MODE = "purchase_mode";
    public static final String RECOMMENDED_REPLENISHMENT_QTY = "recommended_replenishment_qty";
    public static final String TOTAL_STOCK_QTY = "total_stock_qty";
    public static final String STOCKED_INBOUND_QTY = "stocked_inbound_qty";
    public static final String TRANSFER_ASN_INBOUND_QTY = "transfer_asn_inbound_qty";
    public static final String RESERVED_QTY = "reserved_qty";
    public static final String UNSELLABLE_QTY = "unsellable_qty";
    public static final String IN_TRANSIT_QTY = "in_transit_qty";
    public static final String TURNOVER_DAYS = "turnover_days";
    public static final String WAREHOUSE_INVENTORY_COVERAGE_DAYS = "warehouse_inventory_coverage_days";
    public static final String DAILY_AVG_SALES_QTY = "daily_avg_sales_qty";
    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
