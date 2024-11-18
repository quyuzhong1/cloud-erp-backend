package com.erp.sdk.oms.amz.spapi.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReportFlatFileReturnsMongoDTO implements Serializable {

    private String orderId;
    private String orderDate;
    private String returnRequestDate;
    private String returnRequestStatus;
    private String amazonRmaId;
    private String merchantRmaId;
    private String labelType;
    private String labelCost;
    private String currencyCode;
    private String returnCarrier;
    private String trackingId;
    private String labelToBePaidBy;
    private String aToZClaim;
    private String isPrime;
    private String asin;
    private String merchantSku;
    private String itemName;
    private String returnQuantity;
    private String returnReason;
    private String inPolicy;
    private String returnType;
    private String resolution;
    private String invoiceNumber;
    private String returnDeliveryDate;
    private String orderAmount;
    private String orderQuantity;
    private String safeTActionReason;
    private String safeTClaimId;
    private String safeTClaimState;
    private String safeTClaimCreationTime;
    private String safeTClaimReimbursementAmount;
    private String refundedAmount;

}
