package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("id")
    private long fid;
    @JsonProperty("order_id")
    private long orderId;
    @JsonProperty("payer_id")
    private long payerId;
    @JsonProperty("collector")
    private BuyerBean collector;
    @JsonProperty("card_id")
    private long cardId;
    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("installments")
    private int installments;
    @JsonProperty("issuer_id")
    private String issuerId;
    @JsonProperty("atm_transfer_reference")
    private AtmTransferReferenceBean atmTransferReference;
    @JsonProperty("coupon_id")
    private Object couponId;
    @JsonProperty("activation_uri")
    private Object activationUri;
    @JsonProperty("operation_type")
    private String operationType;
    @JsonProperty("payment_type")
    private String paymentType;
    @JsonProperty("status")
    private String status;
    @JsonProperty("status_code")
    private Object statusCode;
    @JsonProperty("status_detail")
    private String statusDetail;
    @JsonProperty("transaction_amount")
    private double transactionAmount;
    @JsonProperty("taxes_amount")
    private BigDecimal taxesAmount;
    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
    @JsonProperty("coupon_amount")
    private double couponAmount;
    @JsonProperty("overpaid_amount")
    private int overpaidAmount;
    @JsonProperty("total_paid_amount")
    private BigDecimal totalPaidAmount;
    @JsonProperty("installment_amount")
    private int installmentAmount;
    @JsonProperty("deferred_period")
    private Object deferredPeriod;
    @JsonProperty("date_approved")
    private String dateApproved;
    @JsonProperty("authorization_code")
    private String authorizationCode;
    @JsonProperty("transaction_order_id")
    private Object transactionOrderId;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("date_last_modified")
    private String dateLastModified;
    @JsonProperty("available_actions")
    private List<String> availableActions;

}
