package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 获取头程面单文件请求。
 */
@Data
@Builder
public class FirstMileWaybillRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "头程追踪号列表不能为空")
    @JSONField(name = "first_mile_tracking_number_list")
    private List<String> firstMileTrackingNumberList;
}
