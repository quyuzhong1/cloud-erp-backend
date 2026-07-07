package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 头程追踪号信息。
 */
@Data
public class FirstMileTrackingNumber implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    private String status;

    @JSONField(name = "declare_date")
    private String declareDate;
}
