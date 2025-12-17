package com.erp.model.oms.entity;

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
 * 多渠道订单明细
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_multi_channel_detail")
public class SoMultiChannelDetailEntity extends BaseEntity<SoMultiChannelDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 单据明细id
    */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台sku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 平台产品id
    */
    @TableField("platform_spu_no")
    private String platformSpuNo;
    /**
    * 平台产品名称
    */
    @TableField("platform_product_name")
    private String platformProductName;
    /**
    * 销售订单数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
     * 已出库数量
     */
    @TableField("has_outstock_qty")
    private Integer hasOutstockQty;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;

    /**
     * 系统是否已出库(none未出库part部分出库all已出库)
     * OutstockStatusEnum
     */
    @TableField("outstock_status")
    private String outstockStatus;
    /**
     * fba库存id
     */
    @TableField("fba_inventory_id")
    private String fbaInventoryId;

    public static final String MAIN_ID = "main_id";

    public static final String SO_DETAIL_ID = "so_detail_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_SPU_NO = "platform_spu_no";

    public static final String PLATFORM_PRODUCT_NAME = "platform_product_name";

    public static final String QTY = "qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String PRODUCT_NAME = "product_name";

    public static final String FN_SKU = "fn_sku";

    @Override
    public Serializable pkVal() {
        return null;
    }

}