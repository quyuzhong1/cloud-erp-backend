package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class RefundAmountBeanX {
    /**
     * buyer_service_fee : 0.1
     * currency : USD
     * refund_shipping_fee : 0.2
     * refund_subtotal : 1
     * refund_tax : 0.03
     * refund_total : 1.23
     * retail_delivery_fee : 0.1
     */

    @SerializedName("buyer_service_fee")
    private String buyerServiceFee;
    @SerializedName("currency")
    private String currency;
    @SerializedName("refund_shipping_fee")
    private String refundShippingFee;
    @SerializedName("refund_subtotal")
    private String refundSubtotal;
    @SerializedName("refund_tax")
    private String refundTax;
    @SerializedName("refund_total")
    private String refundTotal;
    @SerializedName("retail_delivery_fee")
    private String retailDeliveryFee;

    public String getBuyerServiceFee() {
        return buyerServiceFee;
    }

    public void setBuyerServiceFee(String buyerServiceFee) {
        this.buyerServiceFee = buyerServiceFee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRefundShippingFee() {
        return refundShippingFee;
    }

    public void setRefundShippingFee(String refundShippingFee) {
        this.refundShippingFee = refundShippingFee;
    }

    public String getRefundSubtotal() {
        return refundSubtotal;
    }

    public void setRefundSubtotal(String refundSubtotal) {
        this.refundSubtotal = refundSubtotal;
    }

    public String getRefundTax() {
        return refundTax;
    }

    public void setRefundTax(String refundTax) {
        this.refundTax = refundTax;
    }

    public String getRefundTotal() {
        return refundTotal;
    }

    public void setRefundTotal(String refundTotal) {
        this.refundTotal = refundTotal;
    }

    public String getRetailDeliveryFee() {
        return retailDeliveryFee;
    }

    public void setRetailDeliveryFee(String retailDeliveryFee) {
        this.retailDeliveryFee = retailDeliveryFee;
    }
}
