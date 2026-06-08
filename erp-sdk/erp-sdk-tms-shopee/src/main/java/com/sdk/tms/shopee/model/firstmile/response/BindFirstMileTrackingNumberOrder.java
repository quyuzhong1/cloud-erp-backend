package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定头程追踪号订单结果。
 */
@Data
public class BindFirstMileTrackingNumberOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "order_sn")
    private String orderSn;

    @JSONField(name = "package_number")
    private String packageNumber;

    @JSONField(name = "fail_error")
    private String failError;

    @JSONField(name = "fail_message")
    private String failMessage;
}
