package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 解绑指定头程追踪号请求。
 */
@Data
@Builder
public class UnbindFirstMileTrackingNumberRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "头程追踪号不能为空")
    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    @Valid
    @NotEmpty(message = "订单列表不能为空")
    @JSONField(name = "order_list")
    private List<FirstMileOrder> orderList;
}
