package com.sdk.oms.mercado.dto.mercado.order;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
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
    private long paymentId;
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
    private BigDecimal taxesAmount;
    @SerializedName("shipping_cost")
    private BigDecimal shippingCost;
    @SerializedName("coupon_amount")
    private double couponAmount;
    @SerializedName("overpaid_amount")
    private int overpaidAmount;
    @SerializedName("total_paid_amount")
    private BigDecimal totalPaidAmount;
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

}
