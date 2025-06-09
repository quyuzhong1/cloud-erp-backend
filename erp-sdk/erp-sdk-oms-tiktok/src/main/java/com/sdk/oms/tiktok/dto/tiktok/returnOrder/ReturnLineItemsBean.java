package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class ReturnLineItemsBean {
    /**
     * order_line_item_id : 576473917261451851
     * product_image : {"height":200,"url":"https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/004797ebfd8c4d3da2df1cc4bfdb0614~tplv-o3syd03w52-origin-jpeg.jpeg?from=4246405447","width":200}
     * product_name : (SP) [PROMO BUNDLING] NICE Tissue Facial 180s x 5 pcs
     * refund_amount : {"buyer_service_fee":"0.1","currency":"USD","refund_shipping_fee":"0.2","refund_subtotal":"1","refund_tax":"0.03","refund_total":"1.23","retail_delivery_fee":"0.1"}
     * return_line_item_id : 4035227657962164811
     * seller_sku : PUTIH 1 TALI
     * sku_id : 2729382476852921560
     * sku_name : 1#, Standard
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
