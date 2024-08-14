package com.sdk.oms.shopify.api.graphql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ShopifyOrderResponse{

    /**
     * 订单号（手动赋值，新中台用）
     */
    private String orderId;

    /**
     * 税号（手动赋值，新中台用）
     */
    private String taxNo;

    private Data data;

    private Extensions extensions;

    @lombok.Data
    @ToString
    public static class Data {
        private Node node;

        // Getters and Setters

        @lombok.Data
        @ToString
        public static class Node {
            private String id;
            private String name;
            @JsonProperty("localizationExtensions")
            private LocalizationExtensions localizationExtensions;

            @lombok.Data
            @ToString
            public static class LocalizationExtensions {

                private List<Nodes> nodes;

                @lombok.Data
                @ToString
                public static class Nodes {
                    private String countryCode;
                    private String key;
                    private String purpose;
                    private String title;
                    private String value;
                }
            }
        }
    }
}
