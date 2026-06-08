package com.sdk.tms.shopee.model.firstmile.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 头程订单信息。
 */
@Data
@Builder
public class FirstMileOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单号不能为空")
    @JSONField(name = "order_sn")
    private String orderSn;

    @JSONField(name = "package_number")
    private String packageNumber;
}
