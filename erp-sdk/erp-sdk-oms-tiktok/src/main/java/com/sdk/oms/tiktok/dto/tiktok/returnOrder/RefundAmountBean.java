package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class RefundAmountBean {
    /**
     * currency : USD
     * refund_shipping_fee : 7.99
     * refund_subtotal : 20.47
     * refund_tax : 1.62
     * refund_total : 28.46
     */

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
}
