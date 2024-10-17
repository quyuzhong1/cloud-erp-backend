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
 * 三方仓发货单明细
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_warehouse_delivery_detail")
public class ThirdWarehouseDeliveryDetailEntity extends BaseEntity<ThirdWarehouseDeliveryDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 三方仓sku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
     * 平台仓
     */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;

    /**
     * 原skuId
     */
    @TableField("source_sku_id")
    private String sourceSkuId;
    /**
     * 原sku
     */
    @TableField("source_sku_no")
    private String sourceSkuNo;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}