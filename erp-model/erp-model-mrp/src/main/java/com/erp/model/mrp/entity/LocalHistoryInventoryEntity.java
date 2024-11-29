package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 本地仓库存
 * </p>
 *
 * @author Lambda
 * @since 2024-11-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("local_history_inventory")
@EqualsAndHashCode(callSuper = true)
public class LocalHistoryInventoryEntity extends BaseEntity<LocalHistoryInventoryEntity> {

    private static final long serialVersionUID = 5962157482578744006L;
    /**
     * 组织id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 可用数量
     */
    @TableField("usable_qty")
    private Integer usableQty;

    /**
     * 待检库存数量
     */
    @TableField("waitqc_qty")
    private Integer waitqcQty;

    /**
     * 冻结库存数量
     */
    @TableField("frozen_qty")
    private Integer frozenQty;

    /**
     * 采购在途库存数量
     */
    @TableField("purchase_transit_qty")
    private Integer purchaseTransitQty;

    /**
     * 调拨在途数量
     */
    @TableField("transfer_transit_qty")
    private Integer transferTransitQty;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String USABLE_QTY = "usable_qty";

    public static final String WAITQC_QTY = "waitqc_qty";

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String PURCHASE_TRANSIT_QTY = "purchase_transit_qty";

    public static final String TRANSFER_TRANSIT_QTY = "transfer_transit_qty";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
