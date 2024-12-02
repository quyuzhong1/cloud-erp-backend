package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReturnOrdersBean {
    /**
     * arbitration_status : IN_PROGRESS
     * buyer_rejected_partial_refund : true
     * can_buyer_keep_item : true
     * combined_return_id : 4035309948547073951
     * create_time : 1690451136
     * discount_amount : [{"currency":"USD","product_platform_discount":"0.1","product_seller_discount":"0.1","shipping_fee_platform_discount":"0.1","shipping_fee_seller_discount":"0.1"}]
     * handover_method : DROP_OFF
     * is_combined_return : true
     * next_return_id : 4035310341095393463
     * order_id : 577686530908261117
     * partial_refund : {"amount":"10","currency":"IDR"}
     * pre_return_id : 4035310341095393452
     * refund_amount : {"buyer_service_fee":"0.1","currency":"USD","refund_shipping_fee":"0.2","refund_subtotal":"1","refund_tax":"0.03","refund_total":"1.23","retail_delivery_fee":"0.1"}
     * return_id : 4035318504086604100
     * return_line_items : [{"order_line_item_id":"576473917261451851","product_image":{"height":200,"url":"https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/004797ebfd8c4d3da2df1cc4bfdb0614~tplv-o3syd03w52-origin-jpeg.jpeg?from=4246405447","width":200},"product_name":"(SP) [PROMO BUNDLING] NICE Tissue Facial 180s x 5 pcs","refund_amount":{"buyer_service_fee":"0.1","currency":"USD","refund_shipping_fee":"0.2","refund_subtotal":"1","refund_tax":"0.03","refund_total":"1.23","retail_delivery_fee":"0.1"},"return_line_item_id":"4035227657962164811","seller_sku":"PUTIH 1 TALI","sku_id":"2729382476852921560","sku_name":"1#, Standard"}]
     * return_method : SELLER_ARRANGE
     * return_provider_id : TH27014E9R5Q4G
     * return_provider_name : J&T Express
     * return_reason : ecom_order_to_ship_canceled_reason_created_by_mistakes
     * return_reason_text : Order created by mistake
     * return_shipping_document_type : SHIPPING_LABEL
     * return_status : RETURN_OR_REFUND_REQUEST_PENDING
     * return_tracking_number : 213456789098765433456
     * return_type : REFUND
     * return_warehouse_address : {"full_address":"1199 Coleman Ave San Jose, CA 95110"}
     * role : BUYER
     * seller_next_action_response : [{"action":"SELLER_RESPOND_REFUND","deadline":1690554680}]
     * seller_proposed_return_type : PARTIAL_REFUND
     * shipment_type : PLATFORM
     * shipping_fee_amount : [{"buyer_paid_return_shipping_fee":"0.1","currency":"USD","platform_paid_return_shipping_fee":"0.1","seller_paid_return_shipping_fee":"0.1"}]
     * update_time : 1690453136
     */

    @SerializedName("arbitration_status")
    private String arbitrationStatus;
    @SerializedName("buyer_rejected_partial_refund")
    private boolean buyerRejectedPartialRefund;
    @SerializedName("can_buyer_keep_item")
    private boolean canBuyerKeepItem;
    @SerializedName("combined_return_id")
    private String combinedReturnId;
    @SerializedName("create_time")
    private int createTime;
    @SerializedName("handover_method")
    private String handoverMethod;
    @SerializedName("is_combined_return")
    private String isCombinedReturn;
    @SerializedName("next_return_id")
    private String nextReturnId;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("partial_refund")
    private PartialRefundBean partialRefund;
    @SerializedName("pre_return_id")
    private String preReturnId;
    @SerializedName("refund_amount")
    private RefundAmountBean refundAmount;
    @SerializedName("return_id")
    private String returnId;
    @SerializedName("return_method")
    private String returnMethod;
    @SerializedName("return_provider_id")
    private String returnProviderId;
    @SerializedName("return_provider_name")
    private String returnProviderName;
    @SerializedName("return_reason")
    private String returnReason;
    @SerializedName("return_reason_text")
    private String returnReasonText;
    @SerializedName("return_shipping_document_type")
    private String returnShippingDocumentType;
    @SerializedName("return_status")
    private String returnStatus;
    @SerializedName("return_tracking_number")
    private String returnTrackingNumber;
    @SerializedName("return_type")
    private String returnType;
    @SerializedName("return_warehouse_address")
    private ReturnWarehouseAddressBean returnWarehouseAddress;
    @SerializedName("role")
    private String role;
    @SerializedName("seller_proposed_return_type")
    private String sellerProposedReturnType;
    @SerializedName("shipment_type")
    private String shipmentType;
    @SerializedName("update_time")
    private int updateTime;
    @SerializedName("discount_amount")
    private List<DiscountAmountBean> discountAmount;
    @SerializedName("return_line_items")
    private List<ReturnLineItemsBean> returnLineItems;
    @SerializedName("seller_next_action_response")
    private List<SellerNextActionResponseBean> sellerNextActionResponse;
    @SerializedName("shipping_fee_amount")
    private List<ShippingFeeAmountBean> shippingFeeAmount;

    public String getArbitrationStatus() {
        return arbitrationStatus;
    }

    public void setArbitrationStatus(String arbitrationStatus) {
        this.arbitrationStatus = arbitrationStatus;
    }

    public boolean isBuyerRejectedPartialRefund() {
        return buyerRejectedPartialRefund;
    }

    public void setBuyerRejectedPartialRefund(boolean buyerRejectedPartialRefund) {
        this.buyerRejectedPartialRefund = buyerRejectedPartialRefund;
    }

    public boolean isCanBuyerKeepItem() {
        return canBuyerKeepItem;
    }

    public void setCanBuyerKeepItem(boolean canBuyerKeepItem) {
        this.canBuyerKeepItem = canBuyerKeepItem;
    }

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

    public String getHandoverMethod() {
        return handoverMethod;
    }

    public void setHandoverMethod(String handoverMethod) {
        this.handoverMethod = handoverMethod;
    }

    public String getIsCombinedReturn() {
        return isCombinedReturn;
    }

    public void setIsCombinedReturn(String isCombinedReturn) {
        this.isCombinedReturn = isCombinedReturn;
    }

    public String getNextReturnId() {
        return nextReturnId;
    }

    public void setNextReturnId(String nextReturnId) {
        this.nextReturnId = nextReturnId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PartialRefundBean getPartialRefund() {
        return partialRefund;
    }

    public void setPartialRefund(PartialRefundBean partialRefund) {
        this.partialRefund = partialRefund;
    }

    public String getPreReturnId() {
        return preReturnId;
    }

    public void setPreReturnId(String preReturnId) {
        this.preReturnId = preReturnId;
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

    public String getReturnMethod() {
        return returnMethod;
    }

    public void setReturnMethod(String returnMethod) {
        this.returnMethod = returnMethod;
    }

    public String getReturnProviderId() {
        return returnProviderId;
    }

    public void setReturnProviderId(String returnProviderId) {
        this.returnProviderId = returnProviderId;
    }

    public String getReturnProviderName() {
        return returnProviderName;
    }

    public void setReturnProviderName(String returnProviderName) {
        this.returnProviderName = returnProviderName;
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

    public String getReturnShippingDocumentType() {
        return returnShippingDocumentType;
    }

    public void setReturnShippingDocumentType(String returnShippingDocumentType) {
        this.returnShippingDocumentType = returnShippingDocumentType;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnTrackingNumber() {
        return returnTrackingNumber;
    }

    public void setReturnTrackingNumber(String returnTrackingNumber) {
        this.returnTrackingNumber = returnTrackingNumber;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public ReturnWarehouseAddressBean getReturnWarehouseAddress() {
        return returnWarehouseAddress;
    }

    public void setReturnWarehouseAddress(ReturnWarehouseAddressBean returnWarehouseAddress) {
        this.returnWarehouseAddress = returnWarehouseAddress;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSellerProposedReturnType() {
        return sellerProposedReturnType;
    }

    public void setSellerProposedReturnType(String sellerProposedReturnType) {
        this.sellerProposedReturnType = sellerProposedReturnType;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
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

    public List<SellerNextActionResponseBean> getSellerNextActionResponse() {
        return sellerNextActionResponse;
    }

    public void setSellerNextActionResponse(List<SellerNextActionResponseBean> sellerNextActionResponse) {
        this.sellerNextActionResponse = sellerNextActionResponse;
    }

    public List<ShippingFeeAmountBean> getShippingFeeAmount() {
        return shippingFeeAmount;
    }

    public void setShippingFeeAmount(List<ShippingFeeAmountBean> shippingFeeAmount) {
        this.shippingFeeAmount = shippingFeeAmount;
    }
}
