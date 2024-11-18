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
 * fba历史库存
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("fba_history_inventory")
@EqualsAndHashCode(callSuper = true)
public class FbaHistoryInventoryEntity extends BaseEntity<FbaHistoryInventoryEntity> {

    private static final long serialVersionUID = 846903016198172338L;
    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 平台sku
     */
    @TableField("asin")
    private String asin;

    /**
     * 卖家sku
     */
    @TableField("msku")
    private String msku;

    /**
     * FNSKU
     */
    @TableField("fn_sku")
    private String fnSku;

    /**
     * ERP的SKU
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * FBM可售
     */
    @TableField("fbm_fulfillable_qty")
    private Integer fbmFulfillableQty;

    /**
     * 计划入库数量
     */
    @TableField("inbound_working_qty")
    private Integer inboundWorkingQty;

    /**
     * 已发货数量
     */
    @TableField("inbound_shipped_qty")
    private Integer inboundShippedQty;

    /**
     * 入库中数量
     */
    @TableField("inbound_receiving_qty")
    private Integer inboundReceivingQty;

    /**
     * FBI可售
     */
    @TableField("fulfillable_qty")
    private Integer fulfillableQty;

    /**
     * 预留
     */
    @TableField("reserved_qty")
    private Integer reservedQty;

    /**
     * 调查中数量
     */
    @TableField("researching_qty")
    private Integer researchingQty;

    /**
     * 不可售数量
     */
    @TableField("unsellable_qty")
    private Integer unsellableQty;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String FBM_FULFILLABLE_QTY = "fbm_fulfillable_qty";

    public static final String INBOUND_WORKING_QTY = "inbound_working_qty";

    public static final String INBOUND_SHIPPED_QTY = "inbound_shipped_qty";

    public static final String INBOUND_RECEIVING_QTY = "inbound_receiving_qty";

    public static final String FULFILLABLE_QTY = "fulfillable_qty";

    public static final String RESERVED_QTY = "reserved_qty";

    public static final String RESEARCHING_QTY = "researching_qty";

    public static final String UNSELLABLE_QTY = "unsellable_qty";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
