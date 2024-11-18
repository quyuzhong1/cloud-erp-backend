package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class ReturnLineItemsBean {
    /**
     * order_line_item_id : 576636726506918456
     * product_image : {"height":200,"url":"https://p16-oec-sg.ibyteimg.com/tos-alisg-i-aphluv4xwc-sg/10d1df26601e46fab0683718196bc57d~tplv-aphluv4xwc-origin-jpeg.jpeg?from=4246405447","width":200}
     * product_name : Ulanzi MT-44 Extendable Vlog Tripod
     * refund_amount : {"currency":"USD","refund_shipping_fee":"7.99","refund_subtotal":"20.47","refund_tax":"1.62","refund_total":"28.46"}
     * return_line_item_id : 4035240645898834488
     * seller_sku : 2502B
     * sku_id : 1729466135589327821
     * sku_name : Black
     */

    @SerializedName("order_line_item_id")
    private String orderLineItemId;
    @SerializedName("product_image")
    private ProductImageBean productImage;
    @SerializedName("product_name")
    private String productName;
    @SerializedName("refund_amount")
    private RefundAmountBeanX refundAmount;
    @SerializedName("return_line_item_id")
    private String returnLineItemId;
    @SerializedName("seller_sku")
    private String sellerSku;
    @SerializedName("sku_id")
    private String skuId;
    @SerializedName("sku_name")
    private String skuName;

    public String getOrderLineItemId() {
        return orderLineItemId;
    }

    public void setOrderLineItemId(String orderLineItemId) {
        this.orderLineItemId = orderLineItemId;
    }

    public ProductImageBean getProductImage() {
        return productImage;
    }

    public void setProductImage(ProductImageBean productImage) {
        this.productImage = productImage;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public RefundAmountBeanX getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(RefundAmountBeanX refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReturnLineItemId() {
        return returnLineItemId;
    }

    public void setReturnLineItemId(String returnLineItemId) {
        this.returnLineItemId = returnLineItemId;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }
}
