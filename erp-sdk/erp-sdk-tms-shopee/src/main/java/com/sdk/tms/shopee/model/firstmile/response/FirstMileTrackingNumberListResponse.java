package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 头程追踪号列表响应。
 */
@Data
public class FirstMileTrackingNumberListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "first_mile_tracking_number_list")
    private List<FirstMileTrackingNumber> firstMileTrackingNumberList;

    private Boolean more;

    @JSONField(name = "next_cursor")
    private String nextCursor;
}
