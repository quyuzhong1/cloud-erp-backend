package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DMP FBS 库存
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_fbs_inventory")
public class DmpFbsInventoryEntity extends BaseEntity<DmpFbsInventoryEntity> {

    @TableField("input_task_id")
    private String inputTaskId;

    @TableField("convert_id")
    private String convertId;

    @TableField("next_level_id")
    private String nextLevelId;

    @TableField("unique_encrypt")
    private String uniqueEncrypt;

    @TableField("data_encrypt")
    private String dataEncrypt;

    @TableField("row_key")
    private String rowKey;

    @TableField("whs_region")
    private String whsRegion;

    @TableField("warehouse_item_id")
    private String warehouseItemId;

    @TableField("shop_sku_id")
    private String shopSkuId;

    @TableField("item_id")
    private String itemId;

    @TableField("model_id")
    private String modelId;

    @TableField("fbs_sku")
    private String fbsSku;

    @TableField("platform_sku")
    private String platformSku;

    @TableField("platform_product_name")
    private String platformProductName;

    @TableField("spec_name")
    private String specName;

    @TableField("warehouse_id")
    private String warehouseId;

    @TableField("warehouse_name")
    private String warehouseName;

    @TableField("purchase_mode")
    private String purchaseMode;

    @TableField("recommended_replenishment_qty")
    private Integer recommendedReplenishmentQty;

    @TableField("total_stock_qty")
    private Integer totalStockQty;

    @TableField("stocked_inbound_qty")
    private Integer stockedInboundQty;

    @TableField("transfer_asn_inbound_qty")
    private Integer transferAsnInboundQty;

    @TableField("reserved_qty")
    private Integer reservedQty;

    @TableField("unsellable_qty")
    private Integer unsellableQty;

    @TableField("in_transit_qty")
    private Integer inTransitQty;

    @TableField("turnover_days")
    private Integer turnoverDays;

    @TableField("warehouse_inventory_coverage_days")
    private Integer warehouseInventoryCoverageDays;

    @TableField("daily_avg_sales_qty")
    private BigDecimal dailyAvgSalesQty;

    @TableField("last_7_days_sales_qty")
    private Integer last7DaysSalesQty;

    @TableField("last_15_days_sales_qty")
    private Integer last15DaysSalesQty;

    @TableField("last_30_days_sales_qty")
    private Integer last30DaysSalesQty;

    @TableField("last_60_days_sales_qty")
    private Integer last60DaysSalesQty;

    @TableField("last_90_days_sales_qty")
    private Integer last90DaysSalesQty;

    @TableField("stock_age_0_30_qty")
    private Integer stockAge030Qty;

    @TableField("stock_age_31_60_qty")
    private Integer stockAge3160Qty;

    @TableField("stock_age_61_90_qty")
    private Integer stockAge6190Qty;

    @TableField("stock_age_91_120_qty")
    private Integer stockAge91120Qty;

    @TableField("stock_age_121_180_qty")
    private Integer stockAge121180Qty;

    @TableField("stock_age_over_180_qty")
    private Integer stockAgeOver180Qty;

    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
}
