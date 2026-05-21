package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送模式生成并绑定头程追踪号响应。
 */
@Data
public class GenerateAndBindFirstMileTrackingNumberResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "binding_id")
    private String bindingId;

    @JSONField(name = "first_mile_tracking_number")
    private String firstMileTrackingNumber;

    @JSONField(name = "success_list")
    private List<FirstMileBindingOrder> successList;

    @JSONField(name = "fail_list")
    private List<FirstMileBindingFail> failList;
}
