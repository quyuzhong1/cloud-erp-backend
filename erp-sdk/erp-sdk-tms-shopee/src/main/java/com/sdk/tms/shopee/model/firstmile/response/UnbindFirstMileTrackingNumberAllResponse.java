package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 解绑订单头程追踪号或绑定ID响应。
 */
@Data
public class UnbindFirstMileTrackingNumberAllResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "success_list")
    private List<UnbindFirstMileTrackingNumberSuccess> successList;

    @JSONField(name = "fail_list")
    private List<FirstMileBindingFail> failList;
}
