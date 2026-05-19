package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 解绑订单头程追踪号或绑定ID请求。
 */
@Data
@Builder
public class UnbindFirstMileTrackingNumberAllRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Valid
    @NotEmpty(message = "订单列表不能为空")
    @JSONField(name = "order_list")
    private List<FirstMileOrder> orderList;
}
