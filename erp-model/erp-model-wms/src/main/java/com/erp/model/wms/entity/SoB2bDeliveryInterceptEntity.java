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
 * b2b发货拦截单
 * </p>
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2b_delivery_intercept")
public class SoB2bDeliveryInterceptEntity extends BaseEntity<SoB2bDeliveryInterceptEntity> {

    @TableField("code")
    private String code;

    @TableField("source_id")
    private String sourceId;

    @TableField("source_code")
    private String sourceCode;

    @TableField("source_type")
    private String sourceType;

    @TableField("handle_status")
    private String handleStatus;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("so_code")
    private String soCode;

    @TableField("third_delivery_code")
    private String thirdDeliveryCode;

    @TableField("so_outstock_code")
    private String soOutstockCode;

    @TableField("logistics_channel_id")
    private String logisticsChannelId;

    @TableField("logistics_channel_name")
    private String logisticsChannelName;

    @TableField("transport_no")
    private String transportNo;

    @TableField("third_warehouse_order_code")
    private String thirdWarehouseOrderCode;

    @TableField("remark")
    private String remark;

    @TableField("handle_user_id")
    private String handleUserId;

    @TableField("handle_user_name")
    private String handleUserName;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField("cancel_status")
    private String cancelStatus;

    @TableField("intercept_status")
    private String interceptStatus;

    @TableField("so_id")
    private String soId;

    @TableField("bill_type")
    private String billType;

    public static final String SOURCE_ID = "source_id";

    public static final String HANDLE_STATUS = "handle_status";

    @Override
    public Serializable pkVal() {
        return null;
    }
}
