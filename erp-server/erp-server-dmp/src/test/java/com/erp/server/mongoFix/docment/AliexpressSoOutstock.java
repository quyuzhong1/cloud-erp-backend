package com.erp.server.mongoFix.docment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "aliexpress_soOutstock_data")
public class AliexpressSoOutstock {
    @Id
    private String id;
    private String fulfillmentOrderNo;
    private String tradeOrderNo;

    @Data
    @Document(collection = "aliexpress_soOutstockDetail_data")
    public static class AliexpressSoOutstockDetail {
        @Id
        private String id;
        private String fulfillmentOrderNo;
        private String scItemId;
        private String itemId;

        private BigDecimal skuActualPaidAmount;
        private String skuActualPaidCurrency;

        private BigDecimal skuDiscountAmount;
        private String skuDiscountCurrency;

        private BigDecimal unitPrice;
        private String unitPriceCurrency;
    }


    @Data
    public static class MergedOrderData {
        private String fulfillmentOrderNo;
        private String tradeOrderNo;
        private String scItemId;
        private String itemId;
        private String skuId;

        private String skuActualPaidAmount;
        private String skuActualPaidCurrency;

        private String skuDiscountAmount;
        private String skuDiscountCurrency;

        private String unitPrice;
        private String unitPriceCurrency;
    }

}


