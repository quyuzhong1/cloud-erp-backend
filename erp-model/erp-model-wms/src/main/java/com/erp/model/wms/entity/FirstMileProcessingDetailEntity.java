package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 头程虚拟仓订单跟踪明细
 * </p>
 *
 * @author will
 * @since 2025-02-25
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_processing_detail")
public class FirstMileProcessingDetailEntity extends BaseEntity<FirstMileProcessingDetailEntity> {

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
    * 头程发货明细id
    */
    @TableField("first_mile_delivery_detail_id")
    private String firstMileDeliveryDetailId;
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
    @TableField("outstock_order_time")
    private LocalDateTime outstockOrderTime;
    /**
    * 出库数量
    */
    @TableField("outstock_qty")
    private Integer outstockQty;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;


    public static final String FIRST_MILE_DELIVERY_ID = "first_mile_delivery_id";

    public static final String FIRST_MILE_DELIVERY_CODE = "first_mile_delivery_code";

    public static final String FIRST_MILE_DELIVERY_DETAIL_ID = "first_mile_delivery_detail_id";

    public static final String DELIVERY_APPROVE_STATUS = "delivery_approve_status";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String OUTSTOCK_ORDER_ID = "outstock_order_id";

    public static final String OUTSTOCK_ORDER_CODE = "outstock_order_code";

    public static final String OUTSTOCK_ORDER_TYPE = "outstock_order_type";

    public static final String OUTSTOCK_ORDER_STATUS = "outstock_order_status";

    public static final String OUTSTOCK_ORDER_TIME = "outstock_order_time";

    public static final String OUTSTOCK_QTY = "outstock_qty";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}