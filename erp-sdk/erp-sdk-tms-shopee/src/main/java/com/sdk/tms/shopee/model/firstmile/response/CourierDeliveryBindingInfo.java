package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递寄送模式绑定结果。
 */
@Data
public class CourierDeliveryBindingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "binding_id")
    private String bindingId;

    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    private String status;

    private String reason;

    @JSONField(name = "declare_date")
    private String declareDate;
}
