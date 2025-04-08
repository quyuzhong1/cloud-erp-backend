package com.sdk.oms.mercadolocal.dto.mercadolocal.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsBean {
    /**
     * reason : Ulanzi J12 Lavalier Microfone Sem Fio Para iPhone iPad
     * status_code : null
     * total_paid_amount : 170
     * operation_type : regular_payment
     * transaction_amount : 170
     * transaction_amount_refunded : 0
     * date_approved : 2025-02-14T08:26:51.000-04:00
     * collector : {"id":2119968271}
     * coupon_id : null
     * installments : 1
     * authorization_code : null
     * taxes_amount : 0
     * id : 102331168392
     * date_last_modified : 2025-03-05T09:52:38.000-04:00
     * coupon_amount : 0
     * available_actions : ["refund"]
     * shipping_cost : 0
     * installment_amount : null
     * date_created : 2025-02-14T08:26:51.000-04:00
     * activation_uri : null
     * overpaid_amount : 0
     * card_id : null
     * status_detail : accredited
     * issuer_id : 2007
     * payment_method_id : account_money
     * payment_type : account_money
     * deferred_period : null
     * atm_transfer_reference : {"transaction_id":null,"company_id":null}
     * site_id : MLB
     * payer_id : 627939296
     * order_id : 2000010756759156
     * currency_id : BRL
     * status : approved
     * transaction_order_id : null
     */

    @JsonProperty("reason")
    private String reason;
    @JsonProperty("status_code")
    private Object statusCode;
    @JsonProperty("total_paid_amount")
    private BigDecimal totalPaidAmount;
    @JsonProperty("operation_type")
    private String operationType;
    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;
    @JsonProperty("transaction_amount_refunded")
    private int transactionAmountRefunded;
    @JsonProperty("date_approved")
    private String dateApproved;
    @JsonProperty("collector")
    private CollectorBean collector;
    @JsonProperty("coupon_id")
    private Object couponId;
    @JsonProperty("installments")
    private int installments;
    @JsonProperty("authorization_code")
    private Object authorizationCode;
    @JsonProperty("taxes_amount")
    private BigDecimal taxesAmount;
    @JsonProperty("id")
    private long id;
    @JsonProperty("date_last_modified")
    private String dateLastModified;
    @JsonProperty("coupon_amount")
    private BigDecimal couponAmount;
    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
    @JsonProperty("installment_amount")
    private BigDecimal installmentAmount;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("activation_uri")
    private Object activationUri;
    @JsonProperty("overpaid_amount")
    private BigDecimal overpaidAmount;
    @JsonProperty("card_id")
    private Object cardId;
    @JsonProperty("status_detail")
    private String statusDetail;
    @JsonProperty("issuer_id")
    private String issuerId;
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    @JsonProperty("payment_type")
    private String paymentType;
    @JsonProperty("deferred_period")
    private Object deferredPeriod;
    @JsonProperty("atm_transfer_reference")
    private AtmTransferReferenceBean atmTransferReference;
    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("payer_id")
    private long payerId;
    @JsonProperty("order_id")
    private long orderId;
    @JsonProperty("currency_id")
    private String currencyId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("transaction_order_id")
    private Object transactionOrderId;
    @JsonProperty("available_actions")
    private List<String> availableActions;
}
