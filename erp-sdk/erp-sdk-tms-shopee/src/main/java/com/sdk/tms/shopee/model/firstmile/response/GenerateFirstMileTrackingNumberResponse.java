package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 生成头程追踪号响应。
 */
@Data
public class GenerateFirstMileTrackingNumberResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "first_mile_tracking_number_list")
    private List<String> firstMileTrackingNumberList;
}
