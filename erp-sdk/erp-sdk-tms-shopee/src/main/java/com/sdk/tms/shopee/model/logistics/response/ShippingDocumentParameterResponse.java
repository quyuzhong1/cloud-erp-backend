package com.sdk.tms.shopee.model.logistics.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ShippingDocumentParameterResponse

 * @date 2024年10月30日
 * @version: 1.0
 */
@Data
public class ShippingDocumentParameterResponse implements Serializable {
    /**
     * Shopee's unique identifier for an order.
     */
    @JSONField(name = "order_sn")
    private String orderSn;
    /**
     *
     * Shopee's unique identifier for the package under an order.
     */
    @JSONField(name = "package_number")
    private String packageNumber;
    /**
     * The shipping document type Shopee suggests. If you don't select any shipping document type, Shopee will use this as default shipping document type.
     */
    @JSONField(name = "suggest_shipping_document_type")
    private String suggestShippingDocumentType;
    /**
     * The shipping document type you can select of this order.
     */
    @JSONField(name = "selectable_shipping_document_type")
    private List<String> selectableShippingDocumentType;
    /**
     * The status of the shipping document task you querying with order_sn. Available values: READY， FAILED， PROCESSING
     */
    @JSONField(name = "status")
    private String status;
    /**
     * Indicate error type if one element hit error.
     */
    @JSONField(name = "fail_error")
    private String failError;
    /**
     * Indicate error details if one element hit error.
     */
    @JSONField(name = "fail_message")
    private String failMessage;
}
