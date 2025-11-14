package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * B2C虚拟仓订单跟踪
 * </p>
 *
 * @author will
 * @since 2024-12-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_processing")
public class SoB2cProcessingEntity extends BaseEntity<SoB2cProcessingEntity> {

    /**
    * b2c销售订单id
    */
    @TableField("b2c_so_id")
    private String b2cSoId;
    /**
    * b2c销售订单编码
    */
    @TableField("b2c_so_code")
    private String b2cSoCode;
    /**
    * 发货单id
    */
    @TableField("delivery_id")
    private String deliveryId;
    /**
     * 发货单明细id
     */
    @TableField("delivery_detail_id")
    private String deliveryDetailId;
    /**
    * 发货单号
    */
    @TableField("delivery_code")
    private String deliveryCode;
    /**
    * 发货单状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 冻结时间
    */
    @TableField(value = "frozen_time" , fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime frozenTime;
    /**
    * 冻结数量
    */
    @TableField("frozen_qty")
    private Integer frozenQty;
    /**
    * |出库单据id
    */
    @TableField("outstock_order_id")
    private String outstockOrderId;
    /**
    * |出库单据编码
    */
    @TableField("outstock_order_code")
    private String outstockOrderCode;
    /**
    * |出库单据类型（同sourceType）
    */
    @TableField("outstock_order_type")
    private String outstockOrderType;
    /**
    * |出库单据状态
    */
    @TableField("outstock_order_status")
    private String outstockOrderStatus;
    /**
    * |出库单据时间
    */
    @TableField(value = "outstock_order_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime outstockOrderTime;
    /**
    * 出库数量
    */
    @TableField("outstock_qty")
    private Integer outstockQty;
    /**
    * 父级skuId
    */
    @TableField("parent_sku_id")
    private String parentSkuId;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;

    /**
     * 单据时间
     */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 拣货明细id
     */
    @TableField(exist = false)
    private String pickDetailId;

    /**
     * 发货时间
     */
    @TableField(exist = false)
    private LocalDateTime deliveryTime;

    /**
     * 是否存在差异
     */
    @TableField(exist = false)
    private Boolean isDiff;

    public static final String B2C_SO_ID = "b2c_so_id";

    public static final String B2C_SO_CODE = "b2c_so_code";

    public static final String DELIVERY_ID = "delivery_id";

    public static final String DELIVERY_CODE = "delivery_code";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String SKU_ID = "sku_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String FROZEN_TIME = "frozen_time";

    public static final String FROZEN_QTY = "frozen_qty";

    public static final String OUTSTOCK_ORDER_ID = "outstock_order_id";

    public static final String OUTSTOCK_ORDER_CODE = "outstock_order_code";

    public static final String OUTSTOCK_ORDER_TYPE = "outstock_order_type";

    public static final String OUTSTOCK_ORDER_STATUS = "outstock_order_status";

    public static final String OUTSTOCK_ORDER_TIME = "outstock_order_time";

    public static final String OUTSTOCK_QTY = "outstock_qty";

    public static final String PARENT_SKU_ID = "parent_sku_id";

    public static final String BOM_VERSION = "bom_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "SoB2cProcessingEntity{" +
                "b2cSoId='" + b2cSoId + '\'' +
                ", b2cSoCode='" + b2cSoCode + '\'' +
                ", deliveryId='" + deliveryId + '\'' +
                ", deliveryDetailId='" + deliveryDetailId + '\'' +
                ", deliveryCode='" + deliveryCode + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", deliveryQty=" + deliveryQty +
                ", skuId='" + skuId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", virtualWarehouseId='" + virtualWarehouseId + '\'' +
                ", frozenTime=" + frozenTime +
                ", frozenQty=" + frozenQty +
                ", outstockOrderId='" + outstockOrderId + '\'' +
                ", outstockOrderCode='" + outstockOrderCode + '\'' +
                ", outstockOrderType='" + outstockOrderType + '\'' +
                ", outstockOrderStatus='" + outstockOrderStatus + '\'' +
                ", outstockOrderTime=" + outstockOrderTime +
                ", outstockQty=" + outstockQty +
                ", parentSkuId='" + parentSkuId + '\'' +
                ", bomVersion='" + bomVersion + '\'' +
                '}';
    }
}