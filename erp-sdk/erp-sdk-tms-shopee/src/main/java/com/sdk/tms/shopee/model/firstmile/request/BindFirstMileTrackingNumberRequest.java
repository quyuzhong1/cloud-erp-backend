package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 绑定头程追踪号请求。
 */
@Data
@Builder
public class BindFirstMileTrackingNumberRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "头程追踪号不能为空")
    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    @Valid
    @NotEmpty(message = "订单列表不能为空")
    @JSONField(name = "order_list")
    private List<FirstMileOrder> orderList;

    @NotBlank(message = "发货方式不能为空")
    @JSONField(name = "shipment_method")
    private String shipmentMethod;

    @JSONField(name = "logistics_channel_id")
    private Integer logisticsChannelId;

    @NotBlank(message = "发货区域不能为空")
    private String region;

    private BigDecimal weight;

    private BigDecimal volume;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    @JSONField(name = "warehouse_id")
    private String warehouseId;

    @JSONField(name = "warehouse_type")
    private Integer warehouseType;
}
