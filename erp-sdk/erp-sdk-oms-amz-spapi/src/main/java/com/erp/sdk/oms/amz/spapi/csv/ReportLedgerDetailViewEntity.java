package com.erp.sdk.oms.amz.spapi.csv;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ReportLedgerDetailViewEntity {

    private String date;
    private String fnsku;
    private String asin;
    private String msku;
    private String title;
    private String eventType;
    private String referenceID;
    private Integer quantity;
    private String fulfillmentCenter;
    private String disposition;
    private String reason;
    private String country;
    private String reconciledQuantity;
    private String unreconciledQuantity;
    private String dateAndTime;
}
