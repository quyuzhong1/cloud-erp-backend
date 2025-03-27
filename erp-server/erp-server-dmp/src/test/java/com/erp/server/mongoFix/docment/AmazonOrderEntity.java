package com.erp.server.mongoFix.docment;

import lombok.Data;

@Data
public class AmazonOrderEntity {
    private String platformShopCode;
    private String amazonOrderId;
    private String shopId;
    private String all_amount;
    private String total_discount;
}