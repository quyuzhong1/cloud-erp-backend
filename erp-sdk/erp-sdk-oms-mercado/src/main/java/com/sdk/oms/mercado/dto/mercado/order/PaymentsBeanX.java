package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentsBeanX {
    /**
     * id : 72480387497
     * order_id : 2000007633674134
     * payer_id : 139133205
     * collector : {"id":1511265855}
     * card_id : 0
     * site_id : MLM
     * reason : Ulanzi R099 Kit De Montaje Con Clip Para Cámara Gopro
     * payment_method_id : consumer_credits
     * currency_id : USD
     * installments : 1
     * issuer_id :
     * atm_transfer_reference : {"company_id":null,"transaction_id":"611672965"}
     * coupon_id : null
     * activation_uri : null
     * operation_type : regular_payment
     * payment_type : digital_currency
     * available_actions : ["refund"]
     * status : approved
     * status_code : null
     * status_detail : accredited
     * transaction_amount : 21.2
     * taxes_amount : 0
     * shipping_cost : 0
     * coupon_amount : 2.12
     * overpaid_amount : 0
     * total_paid_amount : 19.08
     * installment_amount : 0
     * deferred_period : null
     * date_approved : 2024-02-18T22:28:19.000-04:00
     * authorization_code :
     * transaction_order_id : null
     * date_created : 2024-02-18T22:28:19.000-04:00
     * date_last_modified : 2024-03-01T12:34:02.000-04:00
     */

    @SerializedName("id")
    private long id;
    @SerializedName("order_id")
    private long orderId;
    @SerializedName("payer_id")
    private int payerId;
    @SerializedName("collector")
    private BuyerBean collector;
    @SerializedName("card_id")
    private int cardId;
    @SerializedName("site_id")
    private String siteId;
    @SerializedName("reason")
    private String reason;
    @SerializedName("payment_method_id")
    private String paymentMethodId;
    @SerializedName("currency_id")
    private String currencyId;
    @SerializedName("installments")
    private int installments;
    @SerializedName("issuer_id")
    private String issuerId;
    @SerializedName("atm_transfer_reference")
    private AtmTransferReferenceBean atmTransferReference;
    @SerializedName("coupon_id")
    private Object couponId;
    @SerializedName("activation_uri")
    private Object activationUri;
    @SerializedName("operation_type")
    private String operationType;
    @SerializedName("payment_type")
    private String paymentType;
    @SerializedName("status")
    private String status;
    @SerializedName("status_code")
    private Object statusCode;
    @SerializedName("status_detail")
    private String statusDetail;
    @SerializedName("transaction_amount")
    private double transactionAmount;
    @SerializedName("taxes_amount")
    private int taxesAmount;
    @SerializedName("shipping_cost")
    private int shippingCost;
    @SerializedName("coupon_amount")
    private double couponAmount;
    @SerializedName("overpaid_amount")
    private int overpaidAmount;
    @SerializedName("total_paid_amount")
    private double totalPaidAmount;
    @SerializedName("installment_amount")
    private int installmentAmount;
    @SerializedName("deferred_period")
    private Object deferredPeriod;
    @SerializedName("date_approved")
    private String dateApproved;
    @SerializedName("authorization_code")
    private String authorizationCode;
    @SerializedName("transaction_order_id")
    private Object transactionOrderId;
    @SerializedName("date_created")
    private String dateCreated;
    @SerializedName("date_last_modified")
    private String dateLastModified;
    @SerializedName("available_actions")
    private List<String> availableActions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getPayerId() {
        return payerId;
    }

    public void setPayerId(int payerId) {
        this.payerId = payerId;
    }

    public BuyerBean getCollector() {
        return collector;
    }

    public void setCollector(BuyerBean collector) {
        this.collector = collector;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public int getInstallments() {
        return installments;
    }

    public void setInstallments(int installments) {
        this.installments = installments;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public AtmTransferReferenceBean getAtmTransferReference() {
        return atmTransferReference;
    }

    public void setAtmTransferReference(AtmTransferReferenceBean atmTransferReference) {
        this.atmTransferReference = atmTransferReference;
    }

    public Object getCouponId() {
        return couponId;
    }

    public void setCouponId(Object couponId) {
        this.couponId = couponId;
    }

    public Object getActivationUri() {
        return activationUri;
    }

    public void setActivationUri(Object activationUri) {
        this.activationUri = activationUri;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Object statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public int getTaxesAmount() {
        return taxesAmount;
    }

    public void setTaxesAmount(int taxesAmount) {
        this.taxesAmount = taxesAmount;
    }

    public int getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(int shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(double couponAmount) {
        this.couponAmount = couponAmount;
    }

    public int getOverpaidAmount() {
        return overpaidAmount;
    }

    public void setOverpaidAmount(int overpaidAmount) {
        this.overpaidAmount = overpaidAmount;
    }

    public double getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(double totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public int getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(int installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public Object getDeferredPeriod() {
        return deferredPeriod;
    }

    public void setDeferredPeriod(Object deferredPeriod) {
        this.deferredPeriod = deferredPeriod;
    }

    public String getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(String dateApproved) {
        this.dateApproved = dateApproved;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public Object getTransactionOrderId() {
        return transactionOrderId;
    }

    public void setTransactionOrderId(Object transactionOrderId) {
        this.transactionOrderId = transactionOrderId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(String dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<String> availableActions) {
        this.availableActions = availableActions;
    }
}
