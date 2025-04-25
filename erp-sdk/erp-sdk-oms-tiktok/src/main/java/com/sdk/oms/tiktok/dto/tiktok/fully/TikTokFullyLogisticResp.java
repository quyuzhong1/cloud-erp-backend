package com.sdk.oms.tiktok.dto.tiktok.fully;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyLogisticResp {

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
        @JsonProperty("logistics_orders")
        private List<LogisticsOrdersDTO> logisticsOrders;

        @NoArgsConstructor
        @Data
        public static class LogisticsOrdersDTO {
            @JsonProperty("code")
            private String code;
            @JsonProperty("type")
            private String type;
            @JsonProperty("status")
            private String status;
            @JsonProperty("create_time")
            private Integer createTime;
            @JsonProperty("predicted_ship_time")
            private Integer predictedShipTime;
            @JsonProperty("predicted_arrive_time")
            private Integer predictedArriveTime;
            @JsonProperty("predicted_pick_info")
            private PredictedPickInfoDTO predictedPickInfo;
            @JsonProperty("completion_time")
            private Integer completionTime;
            @JsonProperty("pickup_time")
            private Integer pickupTime;
            @JsonProperty("delivery_time")
            private Integer deliveryTime;
            @JsonProperty("sign_time")
            private Integer signTime;
            @JsonProperty("latest_update_time")
            private Integer latestUpdateTime;
            @JsonProperty("logistics_picker_name")
            private String logisticsPickerName;
            @JsonProperty("logistics_picker_phone")
            private String logisticsPickerPhone;
            @JsonProperty("logistics_sub_order_quantity")
            private Integer logisticsSubOrderQuantity;
            @JsonProperty("logistics_package_quantity")
            private Integer logisticsPackageQuantity;
            @JsonProperty("logistics")
            private LogisticsDTO logistics;
            @JsonProperty("logistics_sub_orders")
            private List<LogisticsSubOrdersDTO> logisticsSubOrders;
            @JsonProperty("sender_contact")
            private SenderContactDTO senderContact;
            @JsonProperty("receiver_contact")
            private ReceiverContactDTO receiverContact;

            @NoArgsConstructor
            @Data
            public static class PredictedPickInfoDTO {
                @JsonProperty("start_time")
                private Integer startTime;
                @JsonProperty("end_time")
                private Integer endTime;
            }

            @NoArgsConstructor
            @Data
            public static class LogisticsDTO {
                @JsonProperty("delivery_option")
                private String deliveryOption;
                @JsonProperty("shipping_provider_name")
                private String shippingProviderName;
                @JsonProperty("shipping_provider_code")
                private String shippingProviderCode;
            }

            @NoArgsConstructor
            @Data
            public static class SenderContactDTO {
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
            public static class ReceiverContactDTO {
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
            public static class LogisticsSubOrdersDTO {
                @JsonProperty("code")
                private String code;
                @JsonProperty("status")
                private String status;
                @JsonProperty("failed_reason")
                private String failedReason;
                @JsonProperty("create_time")
                private Integer createTime;
                @JsonProperty("completion_time")
                private Integer completionTime;
                @JsonProperty("pickup_time")
                private Integer pickupTime;
                @JsonProperty("delivery_time")
                private Integer deliveryTime;
                @JsonProperty("sign_time")
                private Integer signTime;
                @JsonProperty("loss_suspend_time")
                private Integer lossSuspendTime;
                @JsonProperty("latest_update_time")
                private Integer latestUpdateTime;
                @JsonProperty("package_weight")
                private PackageWeightDTO packageWeight;
                @JsonProperty("tracking_number")
                private String trackingNumber;
                @JsonProperty("tracking_records")
                private List<TrackingRecordsDTO> trackingRecords;

                @NoArgsConstructor
                @Data
                public static class PackageWeightDTO {
                    @JsonProperty("value")
                    private String value;
                    @JsonProperty("unit")
                    private String unit;
                }

                @NoArgsConstructor
                @Data
                public static class TrackingRecordsDTO {
                    @JsonProperty("title")
                    private String title;
                    @JsonProperty("message")
                    private String message;
                    @JsonProperty("update_time")
                    private Integer updateTime;
                }
            }
        }
    }
}
