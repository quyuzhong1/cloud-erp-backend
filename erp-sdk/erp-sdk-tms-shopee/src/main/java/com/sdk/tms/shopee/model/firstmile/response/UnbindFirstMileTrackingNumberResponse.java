package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 解绑指定头程追踪号响应。
 */
@Data
public class UnbindFirstMileTrackingNumberResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    @JSONField(name = "order_list")
    private List<UnbindFirstMileTrackingNumberOrder> orderList;
}
