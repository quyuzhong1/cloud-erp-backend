package com.erp.sdk.oms.amz.spapi.csv;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * 亚马逊物流销售报告实体
 */
@Data
@NoArgsConstructor
public class ReportFulfilledShipmentsCsvEntity implements Serializable {

    private String amazonReportId;

    private String amazonOrderId;

    private String merchantOrderId;

    private String shipmentId;

    private String shipmentItemId;

    private String amazonOrderItemId;

    private String merchantOrderItemId;

    private String purchaseDate;

    private String paymentsDate;

    private String shipmentDate;

    private String reportingDate;

    private String buyerEmail;

    private String buyerName;

    private String buyerPhoneNumber;

    private String productName;

    private String quantityShipped;

    private String itemPrice;

    private String itemTax;

    private String shippingPrice;

    private String shippingTax;

    private String giftWrapPrice;

    private String giftWrapTax;

    private String shipServiceLevel;

    private String recipientName;

    private String shipAddress1;

    private String shipAddress2;

    private String shipAddress3;

    private String shipCity;

    private String shipState;

    private String shipPostalCode;

    private String shipCountry;

    private String shipPhoneNumber;

    private String billAddress1;

    private String billAddress2;

    private String billAddress3;

    private String billCity;

    private String billState;

    private String billPostalCode;

    private String billCountry;

    private String itemPromotionDiscount;

    private String shipPromotionDiscount;

    private String trackingNumber;

    private String estimatedArrivalDate;

    private String fulfillmentCenterId;

    private String fulfillmentChannel;

    private String salesChannel;

    private String pointsGranted;

    public String convertShipmentDate(){
        if (StringUtils.isBlank(this.shipmentDate)){
            return "";
        }
        return OffsetDateTime.parse(this.shipmentDate).toLocalDate().toString();
    }
}
