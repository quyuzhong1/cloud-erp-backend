package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 头程绑定成功订单。
 */
@Data
public class FirstMileBindingOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "order_sn")
    private String orderSn;

    @JSONField(name = "package_number")
    private String packageNumber;
}
