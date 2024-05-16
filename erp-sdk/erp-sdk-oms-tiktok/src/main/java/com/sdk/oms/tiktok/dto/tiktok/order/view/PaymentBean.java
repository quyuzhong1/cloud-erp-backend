package com.sdk.oms.tiktok.dto.tiktok.order.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentBean {
    /**
     * currency : IDR
     * original_shipping_fee : 5000
     * original_total_product_price : 5000
     * platform_discount : 5000
     * product_tax : 21.3
     * retail_delivery_fee : 1.28
     * seller_discount : 5000
     * shipping_fee : 5000
     * shipping_fee_platform_discount : 5000
     * shipping_fee_seller_discount : 5000
     * shipping_fee_tax : 11
     * small_order_fee : 3000
     * sub_total : 5000
     * tax : 5000
     * total_amount : 5000
     */

    @JsonProperty("currency")
    private String currency;
    @JsonProperty("original_shipping_fee")
    private String originalShippingFee;
    @JsonProperty("original_total_product_price")
    private String originalTotalProductPrice;
    @JsonProperty("platform_discount")
    private String platformDiscount;
    @JsonProperty("product_tax")
    private String productTax;
    @JsonProperty("retail_delivery_fee")
    private String retailDeliveryFee;
    @JsonProperty("seller_discount")
    private String sellerDiscount;
    @JsonProperty("shipping_fee")
    private String shippingFee;
    @JsonProperty("shipping_fee_platform_discount")
    private String shippingFeePlatformDiscount;
    @JsonProperty("shipping_fee_seller_discount")
    private String shippingFeeSellerDiscount;
    @JsonProperty("shipping_fee_tax")
    private String shippingFeeTax;
    @JsonProperty("small_order_fee")
    private String smallOrderFee;
    @JsonProperty("sub_total")
    private String subTotal;
    @JsonProperty("tax")
    private String tax;
    @JsonProperty("total_amount")
    private String totalAmount;
}
