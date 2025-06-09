package com.sdk.oms.tiktok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FullyDeliveryOrderDTO {

    @JsonProperty("code")
    private Integer code;
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
        @JsonProperty("delivery_orders")
        private List<DeliveryOrdersDTO> deliveryOrders;

        @NoArgsConstructor
        @Data
        public static class DeliveryOrdersDTO {
            @JsonProperty("code")
            private String code;
            @JsonProperty("delivery_type")
            private String deliveryType;
            @JsonProperty("mode")
            private String mode;
            @JsonProperty("relative_code_type")
            private String relativeCodeType;
            @JsonProperty("relative_code")
            private String relativeCode;
            @JsonProperty("stockup_type")
            private String stockupType;
            @JsonProperty("is_first_order")
            private Boolean isFirstOrder;
            @JsonProperty("emergency_level")
            private String emergencyLevel;
            @JsonProperty("status")
            private String status;
            @JsonProperty("quality_check_result")
            private String qualityCheckResult;
            @JsonProperty("platform_spu_code")
            private String platformSpuCode;
            @JsonProperty("category_id")
            private String categoryId;
            @JsonProperty("skc")
            private SkcDTO skc;
            @JsonProperty("delivered_quantity")
            private Integer deliveredQuantity;
            @JsonProperty("received_quantity")
            private Integer receivedQuantity;
            @JsonProperty("inbound_quantity")
            private Integer inboundQuantity;
            @JsonProperty("qualified_quantity")
            private Integer qualifiedQuantity;
            @JsonProperty("unqualified_quantity")
            private Integer unqualifiedQuantity;
            @JsonProperty("returned_quantity")
            private Integer returnedQuantity;
            @JsonProperty("skus")
            private List<SkusDTO> skus;
            @JsonProperty("sample_code")
            private String sampleCode;
            @JsonProperty("sample_status")
            private String sampleStatus;
            @JsonProperty("is_sample_included")
            private Boolean isSampleIncluded;
            @JsonProperty("warehouse_code")
            private String warehouseCode;
            @JsonProperty("warehouse_name")
            private String warehouseName;
            @JsonProperty("warehouse_contact")
            private WarehouseContactDTO warehouseContact;
            @JsonProperty("require_arrived_time")
            private Integer requireArrivedTime;
            @JsonProperty("predicted_arrived_time")
            private Integer predictedArrivedTime;
            @JsonProperty("predicted_ship_time")
            private Integer predictedShipTime;
            @JsonProperty("ship_time")
            private Integer shipTime;
            @JsonProperty("arrived_time")
            private Integer arrivedTime;
            @JsonProperty("latest_status_update_time")
            private Integer latestStatusUpdateTime;
            @JsonProperty("logistics")
            private LogisticsDTO logistics;
            @JsonProperty("delivery_packages")
            private List<DeliveryPackagesDTO> deliveryPackages;

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
                @JsonProperty("first_key_attribute_value_en")
                private String firstKeyAttributeValueEn;
                @JsonProperty("first_key_attribute_value_zh")
                private String firstKeyAttributeValueZh;
            }

            @NoArgsConstructor
            @Data
            public static class WarehouseContactDTO {
                @JsonProperty("contact_name")
                private String contactName;
                @JsonProperty("postal_code")
                private String postalCode;
                @JsonProperty("phone_number")
                private String phoneNumber;
                @JsonProperty("email")
                private String email;
                @JsonProperty("full_address")
                private String fullAddress;
                @JsonProperty("address_detail")
                private AddressDetailDTO addressDetail;

                @NoArgsConstructor
                @Data
                public static class AddressDetailDTO {
                    @JsonProperty("country_name")
                    private String countryName;
                    @JsonProperty("province_name")
                    private String provinceName;
                    @JsonProperty("city_name")
                    private String cityName;
                    @JsonProperty("district_name")
                    private String districtName;
                    @JsonProperty("town_name")
                    private String townName;
                    @JsonProperty("detail")
                    private String detail;
                }
            }

            @NoArgsConstructor
            @Data
            public static class LogisticsDTO {
                @JsonProperty("delivery_option")
                private String deliveryOption;
                @JsonProperty("shipping_provider_code")
                private String shippingProviderCode;
                @JsonProperty("logistics_order")
                private String logisticsOrder;
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
                @JsonProperty("delivered_quantity")
                private Integer deliveredQuantity;
                @JsonProperty("received_quantity")
                private Integer receivedQuantity;
                @JsonProperty("inbound_quantity")
                private Integer inboundQuantity;
                @JsonProperty("qualified_quantity")
                private Integer qualifiedQuantity;
                @JsonProperty("unqualified_quantity")
                private Integer unqualifiedQuantity;
                @JsonProperty("returned_quantity")
                private Integer returnedQuantity;
            }

            @NoArgsConstructor
            @Data
            public static class DeliveryPackagesDTO {
                @JsonProperty("package_code")
                private String packageCode;
                @JsonProperty("skus")
                private List<SkusDTO> skus;

                @NoArgsConstructor
                @Data
                public static class SkusDTO {
                    @JsonProperty("sku_code")
                    private String skuCode;
                    @JsonProperty("quantity")
                    private Integer quantity;
                }
            }
        }
    }
}
