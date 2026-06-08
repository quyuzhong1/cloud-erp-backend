package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 生成头程追踪号请求。
 */
@Data
@Builder
public class GenerateFirstMileTrackingNumberRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "申报日期不能为空")
    @JSONField(name = "declare_date")
    private String declareDate;

    @Min(value = 1, message = "头程追踪号生成数量不能小于1")
    @Max(value = 20, message = "头程追踪号生成数量不能大于20")
    private Integer quantity;
}
