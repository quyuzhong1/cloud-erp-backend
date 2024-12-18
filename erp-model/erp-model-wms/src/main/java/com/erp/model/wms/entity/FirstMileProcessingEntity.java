package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 头程虚拟仓订单跟踪
 * </p>
 *
 * @author will
 * @since 2024-12-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_processing")
public class FirstMileProcessingEntity extends BaseEntity<FirstMileProcessingEntity> {

    /**
    * 要货申请单id
    */
    @TableField("requisition_application_id")
    private String requisitionApplicationId;
    /**
    * 要货申请编码
    */
    @TableField("requisition_application_code")
    private String requisitionApplicationCode;
    /**
    * 头程发货单id
    */
    @TableField("first_mile_delivery_id")
    private String firstMileDeliveryId;
    /**
    * 头程发货单编码
    */
    @TableField("first_mile_delivery_code")
    private String firstMileDeliveryCode;
    /**
    * 头程发货单审核状态
    */
    @TableField("delivery_approve_status")
    private String deliveryApproveStatus;
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
    @TableField("frozen_time")
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
    @TableField("outstock_order_time")
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
    * 批准数量
    */
    @TableField("approve_qty")
    private Integer approveQty;


    public static final String REQUISITION_APPLICATION_ID = "requisition_application_id";

    public static final String REQUISITION_APPLICATION_CODE = "requisition_application_code";

    public static final String FIRST_MILE_DELIVERY_ID = "first_mile_delivery_id";

    public static final String FIRST_MILE_DELIVERY_CODE = "first_mile_delivery_code";

    public static final String DELIVERY_APPROVE_STATUS = "delivery_approve_status";

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

    public static final String APPROVE_QTY = "approve_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}