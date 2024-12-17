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
 * 
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_transit_calculate_detail_report")
public class FbaTransitCalculateDetailReportEntity extends BaseEntity<FbaTransitCalculateDetailReportEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 平台产品id（ASIN）
    */
    @TableField("asin")
    private String asin;
    /**
    * 平台sku（msku）
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
    @TableField("sku_id")
    private String skuId;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 收发差异
    */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
    * 收货数量
    */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 期初在途数量
    */
    @TableField("init_transit_qty")
    private Integer initTransitQty;
    /**
    * 本期发货数量
    */
    @TableField("current_delivery_qty")
    private Integer currentDeliveryQty;
    /**
    * 本期签收数量
    */
    @TableField("current_receive_qty")
    private Integer currentReceiveQty;
    /**
    * 期末在途数量
    */
    @TableField("end_period_transit_qty")
    private Integer endPeriodTransitQty;
    /**
    * 期末在途调整数量
    */
    @TableField("end_period_transit_adjust_qty")
    private Integer endPeriodTransitAdjustQty;
    /**
    * 期末在途（调整后）
    */
    @TableField("after_end_period_transit_qty")
    private Integer afterEndPeriodTransitQty;
    /**
    * 调整原因
    */
    @TableField("adjust_reason")
    private String adjustReason;
    /**
    * 调整时间
    */
    @TableField("adjust_time")
    private LocalDateTime adjustTime;
    /**
    * 调整人名称
    */
    @TableField("adjust_user_name")
    private String adjustUserName;
    /**
    * 调整人id
    */
    @TableField("adjust_user_id")
    private String adjustUserId;
    /**
    * 货件明细id
    */
    @TableField("shipment_detail_id")
    private String shipmentDetailId;
    /**
     * 货件创建时间
     */
    @TableField("shipment_create_time")
    private LocalDateTime shipmentCreateTime;
    /**
     * 货件签收时间
     */
    @TableField("shipment_receive_time")
    private LocalDateTime shipmentReceiveTime;


    public static final String MAIN_ID = "main_id";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String INIT_TRANSIT_QTY = "init_transit_qty";

    public static final String CURRENT_DELIVERY_QTY = "current_delivery_qty";

    public static final String CURRENT_RECEIVE_QTY = "current_receive_qty";

    public static final String END_PERIOD_TRANSIT_QTY = "end_period_transit_qty";

    public static final String END_PERIOD_TRANSIT_ADJUST_QTY = "end_period_transit_adjust_qty";

    public static final String AFTER_END_PERIOD_TRANSIT_QTY = "after_end_period_transit_qty";

    public static final String ADJUST_REASON = "adjust_reason";

    public static final String ADJUST_TIME = "adjust_time";

    public static final String ADJUST_USER_NAME = "adjust_user_name";

    public static final String ADJUST_USER_ID = "adjust_user_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}