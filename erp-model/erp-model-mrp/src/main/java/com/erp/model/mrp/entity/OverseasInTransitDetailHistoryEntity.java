package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 海外在途明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("overseas_in_transit_detail_history")
public class OverseasInTransitDetailHistoryEntity extends BaseEntity<OverseasInTransitDetailHistoryEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 发货单id
     */
    @TableField("delivery_plan_id")
    private String deliveryPlanId;

    /**
     * 发货单code
     */
    @TableField("delivery_plan_code")
    private String deliveryPlanCode;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 发货日期
     */
    @TableField("delivery_date")
    private Date deliveryDate;

    /**
     * 发货数量
     */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
     * 签收数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 在途
     */
    @TableField("in_transit_qty")
    private Integer inTransitQty;

    /**
     * 预计可售日期
     */
    @TableField("estimate_sales_date")
    private Date estimateSalesDate;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String DELIVERY_PLAN_ID = "delivery_plan_id";

    public static final String DELIVERY_PLAN_CODE = "delivery_plan_code";

    public static final String STATUS = "status";

    public static final String DELIVERY_DATE = "delivery_date";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String IN_TRANSIT_QTY = "in_transit_qty";

    public static final String ESTIMATE_SALES_DATE = "estimate_sales_date";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
