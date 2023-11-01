package com.erp.model.wms.entity;

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
     * 第三方唯一编码
     */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
    /**
    * 仓库名称
    */
    @TableField("name")
    private String name;
    /**
    * 平台sku
    */
    @TableField("asin")
    private String asin;
    /**
    * 卖家sku
    */
    @TableField("m_sku")
    private String mSku;
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
    * 库龄
    */
    @TableField("inventory_age")
    private String inventoryAge;


    public static final String PLATFORM_CODE = "platform_code";

    public static final String NAME = "name";

    public static final String ASIN = "asin";

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