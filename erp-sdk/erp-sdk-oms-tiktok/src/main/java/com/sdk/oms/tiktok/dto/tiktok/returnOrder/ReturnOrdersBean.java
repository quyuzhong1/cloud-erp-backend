package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReturnOrdersBean {
    /**
     * combined_return_id : 0
     * create_time : 1716502135
     * discount_amount : [{"currency":"USD","product_platform_discount":"2.1","product_seller_discount":"0","shipping_fee_platform_discount":"0","shipping_fee_seller_discount":"0"}]
     * is_combined_return : false
     * order_id : 576636726506721848
     * refund_amount : {"currency":"USD","refund_shipping_fee":"7.99","refund_subtotal":"20.47","refund_tax":"1.62","refund_total":"28.46"}
     * return_id : 4035240645898768952
     * return_line_items : [{"order_line_item_id":"576636726506918456","product_image":{"height":200,"url":"https://p16-oec-sg.ibyteimg.com/tos-alisg-i-aphluv4xwc-sg/10d1df26601e46fab0683718196bc57d~tplv-aphluv4xwc-origin-jpeg.jpeg?from=4246405447","width":200},"product_name":"Ulanzi MT-44 Extendable Vlog Tripod","refund_amount":{"currency":"USD","refund_shipping_fee":"7.99","refund_subtotal":"20.47","refund_tax":"1.62","refund_total":"28.46"},"return_line_item_id":"4035240645898834488","seller_sku":"2502B","sku_id":"1729466135589327821","sku_name":"Black"}]
     * return_reason : ecom_order_delivered_refund_reason_not_received_cb
     * return_reason_text : Package wasn't received
     * return_status : RETURN_OR_REFUND_REQUEST_COMPLETE
     * return_type : REFUND
     * role : BUYER
     * shipping_fee_amount : [{"buyer_paid_return_shipping_fee":"0","currency":"USD","platform_paid_return_shipping_fee":"0","seller_paid_return_shipping_fee":"0"}]
     * update_time : 1716538773
     * arbitration_status : SUPPORT_BUYER
     * next_return_id : 4035244865309937938
     * pre_return_id : 4035249738475802648
     */

    @SerializedName("combined_return_id")
    private String combinedReturnId;
    @SerializedName("create_time")
    private int createTime;
    @SerializedName("is_combined_return")
    private boolean isCombinedReturn;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("refund_amount")
    private RefundAmountBean refundAmount;
    @SerializedName("return_id")
    private String returnId;
    @SerializedName("return_reason")
    private String returnReason;
    @SerializedName("return_reason_text")
    private String returnReasonText;
    @SerializedName("return_status")
    private String returnStatus;
    @SerializedName("return_type")
    private String returnType;
    @SerializedName("role")
    private String role;
    @SerializedName("update_time")
    private int updateTime;
    @SerializedName("arbitration_status")
    private String arbitrationStatus;
    @SerializedName("next_return_id")
    private String nextReturnId;
    @SerializedName("pre_return_id")
    private String preReturnId;
    @SerializedName("discount_amount")
    private List<DiscountAmountBean> discountAmount;
    @SerializedName("return_line_items")
    private List<ReturnLineItemsBean> returnLineItems;
    @SerializedName("shipping_fee_amount")
    private List<ShippingFeeAmountBean> shippingFeeAmount;

    public String getCombinedReturnId() {
        return combinedReturnId;
    }

    public void setCombinedReturnId(String combinedReturnId) {
        this.combinedReturnId = combinedReturnId;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public boolean isIsCombinedReturn() {
        return isCombinedReturn;
    }

    public void setIsCombinedReturn(boolean isCombinedReturn) {
        this.isCombinedReturn = isCombinedReturn;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public RefundAmountBean getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(RefundAmountBean refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReturnId() {
        return returnId;
    }

    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getReturnReasonText() {
        return returnReasonText;
    }

    public void setReturnReasonText(String returnReasonText) {
        this.returnReasonText = returnReasonText;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public String getArbitrationStatus() {
        return arbitrationStatus;
    }

    public void setArbitrationStatus(String arbitrationStatus) {
        this.arbitrationStatus = arbitrationStatus;
    }

    public String getNextReturnId() {
        return nextReturnId;
    }

    public void setNextReturnId(String nextReturnId) {
        this.nextReturnId = nextReturnId;
    }

    public String getPreReturnId() {
        return preReturnId;
    }

    public void setPreReturnId(String preReturnId) {
        this.preReturnId = preReturnId;
    }

    public List<DiscountAmountBean> getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(List<DiscountAmountBean> discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<ReturnLineItemsBean> getReturnLineItems() {
        return returnLineItems;
    }

    public void setReturnLineItems(List<ReturnLineItemsBean> returnLineItems) {
        this.returnLineItems = returnLineItems;
    }

    public List<ShippingFeeAmountBean> getShippingFeeAmount() {
        return shippingFeeAmount;
    }

    public void setShippingFeeAmount(List<ShippingFeeAmountBean> shippingFeeAmount) {
        this.shippingFeeAmount = shippingFeeAmount;
    }
}
