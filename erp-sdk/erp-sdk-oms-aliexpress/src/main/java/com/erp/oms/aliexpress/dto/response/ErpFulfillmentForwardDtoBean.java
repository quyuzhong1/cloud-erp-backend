package com.erp.oms.aliexpress.dto.response;

import com.google.gson.annotations.SerializedName;

public class ErpFulfillmentForwardDtoBean {
    /**
     * trade_order_no : 8183735967757849
     * package_paid_fee : 14.81(USD)
     * send_fulfill_time : 1705357943997
     * buyer_name : J**
     * receiver_mobile : 5546889448
     * order_status : 已签收
     * trade_create_time : 1705357774718
     * warehouse_name : 菜鸟AE烟台001号优选仓
     * tracking_no : CNMEXDSP0000009666
     * lbx_no : LBX03205814709698839
     * receiver_name : Jezreel Camacho Navarrete
     * extend_fields : {}
     * receiver_country : 墨西哥(MX)
     * receiver_phone : +52
     * fulfillment_order_no : WH1801510349156647
     */

    @SerializedName("trade_order_no")
    private String tradeOrderNo;
    @SerializedName("package_paid_fee")
    private String packagePaidFee;
    @SerializedName("send_fulfill_time")
    private long sendFulfillTime;
    @SerializedName("buyer_name")
    private String buyerName;
    @SerializedName("receiver_mobile")
    private String receiverMobile;
    @SerializedName("order_status")
    private String orderStatus;
    @SerializedName("trade_create_time")
    private long tradeCreateTime;
    @SerializedName("warehouse_name")
    private String warehouseName;
    @SerializedName("tracking_no")
    private String trackingNo;
    @SerializedName("lbx_no")
    private String lbxNo;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("extend_fields")
    private String extendFields;
    @SerializedName("receiver_country")
    private String receiverCountry;
    @SerializedName("receiver_phone")
    private String receiverPhone;
    @SerializedName("fulfillment_order_no")
    private String fulfillmentOrderNo;

    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    public void setTradeOrderNo(String tradeOrderNo) {
        this.tradeOrderNo = tradeOrderNo;
    }

    public String getPackagePaidFee() {
        return packagePaidFee;
    }

    public void setPackagePaidFee(String packagePaidFee) {
        this.packagePaidFee = packagePaidFee;
    }

    public long getSendFulfillTime() {
        return sendFulfillTime;
    }

    public void setSendFulfillTime(long sendFulfillTime) {
        this.sendFulfillTime = sendFulfillTime;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public long getTradeCreateTime() {
        return tradeCreateTime;
    }

    public void setTradeCreateTime(long tradeCreateTime) {
        this.tradeCreateTime = tradeCreateTime;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getLbxNo() {
        return lbxNo;
    }

    public void setLbxNo(String lbxNo) {
        this.lbxNo = lbxNo;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getExtendFields() {
        return extendFields;
    }

    public void setExtendFields(String extendFields) {
        this.extendFields = extendFields;
    }

    public String getReceiverCountry() {
        return receiverCountry;
    }

    public void setReceiverCountry(String receiverCountry) {
        this.receiverCountry = receiverCountry;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getFulfillmentOrderNo() {
        return fulfillmentOrderNo;
    }

    public void setFulfillmentOrderNo(String fulfillmentOrderNo) {
        this.fulfillmentOrderNo = fulfillmentOrderNo;
    }
}
