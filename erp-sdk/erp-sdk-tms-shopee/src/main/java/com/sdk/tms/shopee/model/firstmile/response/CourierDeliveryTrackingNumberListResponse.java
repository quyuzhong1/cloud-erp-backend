package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 快递寄送模式绑定结果列表响应。
 */
@Data
public class CourierDeliveryTrackingNumberListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "tracking_number_list")
    private List<CourierDeliveryBindingInfo> trackingNumberList;

    private Boolean more;

    @JSONField(name = "next_cursor")
    private String nextCursor;
}
