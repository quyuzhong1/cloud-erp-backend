package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送模式生成并绑定头程追踪号请求。
 */
@Data
@Builder
public class GenerateAndBindFirstMileTrackingNumberRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "发货方式不能为空")
    @JSONField(name = "shipment_method")
    private String shipmentMethod;

    private String region;

    @Valid
    @NotEmpty(message = "订单列表不能为空")
    @JSONField(name = "order_list")
    private List<FirstMileOrder> orderList;

    @Valid
    @NotNull(message = "快递寄送信息不能为空")
    @JSONField(name = "courier_delivery_info")
    private CourierDeliveryInfo courierDeliveryInfo;
}
