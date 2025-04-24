package com.erp.server.mongoFix.docment;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class AmazonOrderEntity {
    private String platformShopCode;
    private String amazonOrderId;
    private String shopId;
    private String all_amount;
    private String total_discount;

    private BigDecimal itemTax;
    private BigDecimal shippingTax;
    private BigDecimal giftWrapTax;
    private BigDecimal promotionDiscountTax;
}