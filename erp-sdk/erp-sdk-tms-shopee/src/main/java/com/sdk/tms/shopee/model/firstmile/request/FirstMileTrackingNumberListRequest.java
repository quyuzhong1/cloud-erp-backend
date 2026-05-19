package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 获取头程追踪号列表请求。
 */
@Data
@Builder
public class FirstMileTrackingNumberListRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "开始日期不能为空")
    @JSONField(name = "from_date")
    private String fromDate;

    @NotBlank(message = "结束日期不能为空")
    @JSONField(name = "to_date")
    private String toDate;

    @Min(value = 1, message = "分页大小不能小于1")
    @Max(value = 50, message = "分页大小不能大于50")
    @JSONField(name = "page_size")
    private Integer pageSize;

    private String cursor;
}
