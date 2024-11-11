package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class ShippingFeeAmountBean {
    /**
     * buyer_paid_return_shipping_fee : 0
     * currency : USD
     * platform_paid_return_shipping_fee : 0
     * seller_paid_return_shipping_fee : 0
     */

    @SerializedName("buyer_paid_return_shipping_fee")
    private String buyerPaidReturnShippingFee;
    @SerializedName("currency")
    private String currency;
    @SerializedName("platform_paid_return_shipping_fee")
    private String platformPaidReturnShippingFee;
    @SerializedName("seller_paid_return_shipping_fee")
    private String sellerPaidReturnShippingFee;

    public String getBuyerPaidReturnShippingFee() {
        return buyerPaidReturnShippingFee;
    }

    public void setBuyerPaidReturnShippingFee(String buyerPaidReturnShippingFee) {
        this.buyerPaidReturnShippingFee = buyerPaidReturnShippingFee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPlatformPaidReturnShippingFee() {
        return platformPaidReturnShippingFee;
    }

    public void setPlatformPaidReturnShippingFee(String platformPaidReturnShippingFee) {
        this.platformPaidReturnShippingFee = platformPaidReturnShippingFee;
    }

    public String getSellerPaidReturnShippingFee() {
        return sellerPaidReturnShippingFee;
    }

    public void setSellerPaidReturnShippingFee(String sellerPaidReturnShippingFee) {
        this.sellerPaidReturnShippingFee = sellerPaidReturnShippingFee;
    }
}
