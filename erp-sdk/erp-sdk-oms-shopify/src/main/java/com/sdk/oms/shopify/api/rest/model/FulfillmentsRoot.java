package com.sdk.oms.shopify.api.rest.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class FulfillmentsRoot implements Serializable {
    private List<Fulfillment> fulfillments;


    @Data
    @NoArgsConstructor
    public static class Fulfillment implements Serializable {
        private String id;
        private String orderId;
        private String status;
        private String createdAt;
        private String service;
        private String updatedAt;
        private String trackingCompany;
        private String shipmentStatus;
        private String locationId;
        private Object originAddress;
        private List<LineItem> lineItems;
        private String trackingNumber;
        private List<String> trackingNumbers;
        private String trackingUrl;
        private List<String> trackingUrls;
        private Receipt receipt;
        private String name;
        private String adminGraphqlApiId;

    }

    @Data
    @NoArgsConstructor
    public static class LineItem implements Serializable {
        private String id;
        private String variantId;
        private String title;
        private Integer quantity;
        private String sku;
        private String variantTitle;
        private String vendor;
        private String fulfillmentService;
        private String productId;
        private Boolean requiresShipping;
        private Boolean taxable;
        private Boolean giftCard;
        private String name;
        private String variantInventoryManagement;
        private List<Object> properties;
        private Boolean productExists;
        private int fulfillableQuantity;
        private int grams;
        private String price;
        private String totalDiscount;
        private String fulfillmentStatus;
        private PriceSet priceSet;
        private PriceSet totalDiscountSet;
        private List<DiscountAllocation> discountAllocations;
        private OriginLocation originLocation;
        private String adminGraphqlApiId;
        private List<Object> duties;
        private List<TaxLine> taxLines;
        private String fulfillmentLineItemId;

    }

    @Data
    @NoArgsConstructor
    public static class PriceSet implements Serializable {
        private Money shopMoney;
        private Money presentmentMoney;
    }

    @Data
    @NoArgsConstructor
    public static class Money implements Serializable {
        private String amount;
        private String currencyCode;

    }

    @Data
    @NoArgsConstructor
    public static class DiscountAllocation implements Serializable {
        private String amount;
        private int discountApplicationIndex;
        private PriceSet amountSet;

    }

    @Data
    @NoArgsConstructor
    public static class OriginLocation implements Serializable {
        private String id;
        private String countryCode;
        private String provinceCode;
        private String name;
        private String address1;
        private String address2;
        private String city;
        private String zip;

    }

    @Data
    @NoArgsConstructor
    public static class TaxLine implements Serializable {
        private String price;
        private String rate;
        private String title;
        private PriceSet priceSet;
        private Object channelLiable;

    }

    @Data
    @NoArgsConstructor
    public static class Receipt implements Serializable {
        private Boolean testcase;
        // Assuming receipt is an empty object, no fields are defined
        private String authorization;
    }
}

