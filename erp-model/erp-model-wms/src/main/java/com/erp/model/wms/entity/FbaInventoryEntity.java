package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.OffsetDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBI库存
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_inventory")
public class FbaInventoryEntity extends BaseEntity<FbaInventoryEntity> {

    /**
     * 仓库ID
     */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
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
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 配送渠道
    */
    @TableField("delivery_channels")
    private String deliveryChannels;
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
     * 待调仓数量
     */
    @TableField("reserved_transfers_qty")
    private Integer reservedTransfersQty;

    /**
     * 调仓中数量
     */
    @TableField("reserved_processing_qty")
    private Integer reservedProcessingQty;

    /**
     * 买家订单数量
     */
    @TableField("reserved_order_qty")
    private Integer reservedOrderQty;

    /**
     * 数据开始时间
     */
    @TableField("data_start_time")
    private OffsetDateTime dataStartTime;

    /**
     * 数据结束时间
     */
    @TableField("data_end_time")
    private OffsetDateTime dataEndTime;

    /**
     * 库龄 0-30 天的可售商品数量
     */
    @TableField("inventory_age_0_to_30_days")
    private Integer inventoryAge0To30Days;

    /**
     * 库龄 31-60 天的可售商品数量
     */
    @TableField("inventory_age_31_to_60_days")
    private Integer inventoryAge31To60Days;

    /**
     * 库龄 61-90 天的可售商品数量
     */
    @TableField("inventory_age_61_to_90_days")
    private Integer inventoryAge61To90Days;

    /**
     * 库龄 91-180 天的可售商品数量
     */
    @TableField("inventory_age_91_to_180_days")
    private Integer inventoryAge91To180Days;

    /**
     * 库龄 181-270 天的可售商品数量
     */
    @TableField("inventory_age_181_to_270_days")
    private Integer inventoryAge181To270Days;

    /**
     * 库龄 271-365 天的可售商品数量
     */
    @TableField("inventory_age_271_to_365_days")
    private Integer inventoryAge271To365Days;

    /**
     * 库龄 365 天以上的可售商品数量
     */
    @TableField("inventory_age_365_plus_days")
    private Integer inventoryAge365PlusDays;



    public static final String PLATFORM_CODE = "platform_code";

    

    public static final String M_SKU = "m_sku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DELIVERY_CHANNELS = "delivery_channels";

    public static final String FBM_FULFILLABLE_QTY = "fbm_fulfillable_qty";

    public static final String INBOUND_WORKING_QTY = "inbound_working_qty";

    public static final String INBOUND_SHIPPED_QTY = "inbound_shipped_qty";

    public static final String INBOUND_RECEIVING_QTY = "inbound_receiving_qty";

    public static final String FULFILLABLE_QTY = "fulfillable_qty";

    public static final String RESERVED_QTY = "reserved_qty";

    public static final String RESEARCHING_QTY = "researching_qty";

    public static final String UNSELLABLE_QTY = "unsellable_qty";

    public static final String INVENTORY_AGE = "inventory_age";

    @Override
    public Serializable pkVal() {
        return null;
    }

}