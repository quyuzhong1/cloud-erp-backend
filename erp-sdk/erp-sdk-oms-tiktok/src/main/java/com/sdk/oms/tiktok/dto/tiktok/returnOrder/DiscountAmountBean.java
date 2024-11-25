package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class DiscountAmountBean {
    /**
     * currency : USD
     * product_platform_discount : 0.1
     * product_seller_discount : 0.1
     * shipping_fee_platform_discount : 0.1
     * shipping_fee_seller_discount : 0.1
     */

    @SerializedName("currency")
    private String currency;
    @SerializedName("product_platform_discount")
    private String productPlatformDiscount;
    @SerializedName("product_seller_discount")
    private String productSellerDiscount;
    @SerializedName("shipping_fee_platform_discount")
    private String shippingFeePlatformDiscount;
    @SerializedName("shipping_fee_seller_discount")
    private String shippingFeeSellerDiscount;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProductPlatformDiscount() {
        return productPlatformDiscount;
    }

    public void setProductPlatformDiscount(String productPlatformDiscount) {
        this.productPlatformDiscount = productPlatformDiscount;
    }

    public String getProductSellerDiscount() {
        return productSellerDiscount;
    }

    public void setProductSellerDiscount(String productSellerDiscount) {
        this.productSellerDiscount = productSellerDiscount;
    }

    public String getShippingFeePlatformDiscount() {
        return shippingFeePlatformDiscount;
    }

    public void setShippingFeePlatformDiscount(String shippingFeePlatformDiscount) {
        this.shippingFeePlatformDiscount = shippingFeePlatformDiscount;
    }

    public String getShippingFeeSellerDiscount() {
        return shippingFeeSellerDiscount;
    }

    public void setShippingFeeSellerDiscount(String shippingFeeSellerDiscount) {
        this.shippingFeeSellerDiscount = shippingFeeSellerDiscount;
    }
}
