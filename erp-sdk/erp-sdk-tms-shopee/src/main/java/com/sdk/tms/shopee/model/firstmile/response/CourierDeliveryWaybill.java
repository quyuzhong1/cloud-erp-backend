package com.sdk.tms.shopee.model.firstmile.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递寄送模式面单信息。
 */
@Data
public class CourierDeliveryWaybill implements Serializable {
    private static final long serialVersionUID = 1L;

    @JSONField(name = "binding_id")
    private String bindingId;

    @JSONField(name = "shipping_label_url")
    private String shippingLabelUrl;
}
