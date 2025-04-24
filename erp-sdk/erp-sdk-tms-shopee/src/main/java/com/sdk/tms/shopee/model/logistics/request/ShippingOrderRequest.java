package com.sdk.tms.shopee.model.logistics.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ShippingOrderRequest
 * @description: 订单获取面单请求参数
 * @date 2024年10月15日
 * @version: 1.0
 */
@Data
@Builder
public class ShippingOrderRequest implements Serializable {
    /**
     *Shopee's unique identifier for an order.
     */
    @JSONField(name = "order_sn")
    private String orderSn;
    /**
     *Shopee's unique identifier for the package under an order. You should't fill the field with empty string when there is not a package number.
     */
    @JSONField(name = "package_number")
    private String packageNumber;
    /**
     *The tracking number of order. Required except for the channel allow print before arrange shipment.
     */
    @JSONField(name = "tracking_number")
    private String trackingNumber;
    /**
     *The type of shipping document. Available values: NORMAL_AIR_WAYBILL,THERMAL_AIR_WAYBILL,NORMAL_JOB_AIR_WAYBILL,THERMAL_JOB_AIR_WAYBILL
     */
    @JSONField(name = "shipping_document_type")
    private String shippingDocumentType;
}
