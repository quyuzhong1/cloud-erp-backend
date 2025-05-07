package com.sdk.oms.tiktok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FullyOrderDTO {

    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("next_page_token")
        private String nextPageToken;
        @JsonProperty("total_count")
        private Integer totalCount;
        @JsonProperty("stockup_orders")
        private List<StockupOrdersDTO> stockupOrders;

        @NoArgsConstructor
        @Data
        public static class StockupOrdersDTO {
            @JsonProperty("code")
            private String code;
            @JsonProperty("emergency_level")
            private String emergencyLevel;
            @JsonProperty("status")
            private String status;
            @JsonProperty("is_first_order")
            private Boolean isFirstOrder;
            @JsonProperty("type")
            private String type;
            @JsonProperty("source")
            private String source;
            @JsonProperty("manufacture_mode")
            private String manufactureMode;
            @JsonProperty("is_delivery_completed")
            private Boolean isDeliveryCompleted;
            @JsonProperty("is_normal")
            private Boolean isNormal;
            @JsonProperty("can_deliver")
            private Boolean canDeliver;
            @JsonProperty("create_time")
            private Integer createTime;
            @JsonProperty("require_ship_time")
            private Integer requireShipTime;
            @JsonProperty("require_arrived_time")
            private Integer requireArrivedTime;
            @JsonProperty("latest_status_update_time")
            private Integer latestStatusUpdateTime;
            @JsonProperty("platform_spu_code")
            private String platformSpuCode;
            @JsonProperty("category_id")
            private String categoryId;
            @JsonProperty("skc")
            private SkcDTO skc;
            @JsonProperty("stockup_quantity")
            private Integer stockupQuantity;
            @JsonProperty("delivered_quantity")
            private Integer deliveredQuantity;
            @JsonProperty("received_quantity")
            private Integer receivedQuantity;
            @JsonProperty("inbound_quantity")
            private Integer inboundQuantity;
            @JsonProperty("returned_quantity")
            private Integer returnedQuantity;
            @JsonProperty("skus")
            private List<SkusDTO> skus;

            @NoArgsConstructor
            @Data
            public static class SkcDTO {
                @JsonProperty("platform_skc_code")
                private String platformSkcCode;
                @JsonProperty("image_url")
                private String imageUrl;
                @JsonProperty("external_skc_code")
                private String externalSkcCode;
                @JsonProperty("first_key_attribute_name_en")
                private String firstKeyAttributeNameEn;
                @JsonProperty("first_key_attribute_name_zh")
                private String firstKeyAttributeNameZh;
                @JsonProperty("first_key_attribute_value_zh")
                private String firstKeyAttributeValueZh;
            }

            @NoArgsConstructor
            @Data
            public static class SkusDTO {
                @JsonProperty("platform_sku_code")
                private String platformSkuCode;
                @JsonProperty("barcode")
                private String barcode;
                @JsonProperty("external_sku_code")
                private String externalSkuCode;
                @JsonProperty("second_key_attribute_name_en")
                private String secondKeyAttributeNameEn;
                @JsonProperty("second_key_attribute_name_zh")
                private String secondKeyAttributeNameZh;
                @JsonProperty("second_key_attribute_value_en")
                private String secondKeyAttributeValueEn;
                @JsonProperty("second_key_attribute_value_zh")
                private String secondKeyAttributeValueZh;
                @JsonProperty("stockup_quantity")
                private Integer stockupQuantity;
                @JsonProperty("delivered_quantity")
                private Integer deliveredQuantity;
                @JsonProperty("received_quantity")
                private Integer receivedQuantity;
                @JsonProperty("inbound_quantity")
                private Integer inboundQuantity;
                @JsonProperty("returned_quantity")
                private Integer returnedQuantity;
            }
        }
    }
}
