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
 * B2B虚拟仓订单跟踪
 * </p>
 *
 * @author will
 * @since 2024-12-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2b_processing")
public class SoB2bProcessingEntity extends BaseEntity<SoB2bProcessingEntity> {

    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
     * 销售订单明细id
     */
    @TableField("so_detail_id")
    private String soDetailId;
    /**
    * 销售订单编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售订单状态
    */
    @TableField("so_approve_status")
    private String soApproveStatus;
    /**
    * 销售数量
    */
    @TableField("so_qty")
    private Integer soQty;
    /**
    * 发货通知单id
    */
    @TableField("delivery_notice_id")
    private String deliveryNoticeId;
    /**
    * 发货通知单编码
    */
    @TableField("delivery_notice_code")
    private String deliveryNoticeCode;
    /**
    * 发货通知单审核状态
    */
    @TableField("delivery_notice_approve_status")
    private String deliveryNoticeApproveStatus;

    /**
     * 发货通知单明细id
     */
    @TableField("delivery_notice_detail_id")
    private String deliveryNoticeDetailId;

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
    * 出库单据id
    */
    @TableField("outstock_order_id")
    private String outstockOrderId;
    /**
    * 出库单据编码
    */
    @TableField("outstock_order_code")
    private String outstockOrderCode;
    /**
    * 出库单据类型（同sourceType）
    */
    @TableField("outstock_order_type")
    private String outstockOrderType;
    /**
    * 出库单据状态
    */
    @TableField("outstock_order_status")
    private String outstockOrderStatus;
    /**
    * 出库单据时间
    */
    @TableField(value = "outstock_order_time" , fill = FieldFill.INSERT_UPDATE)
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
     * 是否存在差异
     */
    @TableField(exist = false)
    private Boolean isDiff;

    /**
     * 是否需要出库
     */
    @TableField(exist = false)
    private Boolean isOutstock;

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String SO_APPROVE_STATUS = "so_approve_status";

    public static final String SO_QTY = "so_qty";

    public static final String DELIVERY_NOTICE_ID = "delivery_notice_id";

    public static final String DELIVERY_NOTICE_CODE = "delivery_notice_code";

    public static final String DELIVERY_NOTICE_APPROVE_STATUS = "delivery_notice_approve_status";

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
        return "SoB2bProcessingEntity{" +
                "soId='" + soId + '\'' +
                ", soDetailId='" + soDetailId + '\'' +
                ", soCode='" + soCode + '\'' +
                ", soApproveStatus='" + soApproveStatus + '\'' +
                ", soQty=" + soQty +
                ", deliveryNoticeId='" + deliveryNoticeId + '\'' +
                ", deliveryNoticeCode='" + deliveryNoticeCode + '\'' +
                ", deliveryNoticeApproveStatus='" + deliveryNoticeApproveStatus + '\'' +
                ", deliveryNoticeDetailId='" + deliveryNoticeDetailId + '\'' +
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