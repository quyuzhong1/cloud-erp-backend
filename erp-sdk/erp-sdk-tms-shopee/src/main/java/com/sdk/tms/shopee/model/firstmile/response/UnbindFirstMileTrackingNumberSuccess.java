package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 解绑头程追踪号成功订单。
 */
@Data
public class UnbindFirstMileTrackingNumberSuccess implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "order_sn")
    private String orderSn;

    @JSONField(name = "package_number")
    private String packageNumber;

    @JSONField(name = "binding_id")
    private String bindingId;

    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;
}
