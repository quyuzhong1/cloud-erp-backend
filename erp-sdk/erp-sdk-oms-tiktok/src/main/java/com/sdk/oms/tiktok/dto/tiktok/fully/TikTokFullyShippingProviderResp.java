package com.sdk.oms.tiktok.dto.tiktok.fully;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokFullyShippingProviderResp {

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
        @JsonProperty("reserve_arrived_times")
        private List<ReserveArrivedTimesDTO> reserveArrivedTimes;
        @JsonProperty("shipping_providers")
        private List<ShippingProvidersDTO> shippingProviders;
        @JsonProperty("warehouse_list")
        private List<WarehouseListDTO> warehouseList;

        @NoArgsConstructor
        @Data
        public static class ReserveArrivedTimesDTO {
            @JsonProperty("accurate_warehouse_code")
            private String accurateWarehouseCode;
            @JsonProperty("arrived_time")
            private Integer arrivedTime;
            @JsonProperty("can_reserve")
            private Boolean canReserve;
        }

        @NoArgsConstructor
        @Data
        public static class ShippingProvidersDTO {
            @JsonProperty("delivery_option")
            private String deliveryOption;
            @JsonProperty("provider_code")
            private String providerCode;
            @JsonProperty("provider_name")
            private String providerName;
            @JsonProperty("min_charge_fee")
            private MinChargeFeeDTO minChargeFee;
            @JsonProperty("max_charge_fee")
            private MaxChargeFeeDTO maxChargeFee;
            @JsonProperty("reserve_datas")
            private List<ReserveDatasDTO> reserveDatas;

            @NoArgsConstructor
            @Data
            public static class MinChargeFeeDTO {
                @JsonProperty("amount")
                private String amount;
                @JsonProperty("currency")
                private String currency;
            }

            @NoArgsConstructor
            @Data
            public static class MaxChargeFeeDTO {
                @JsonProperty("amount")
                private String amount;
                @JsonProperty("currency")
                private String currency;
            }

            @NoArgsConstructor
            @Data
            public static class ReserveDatasDTO {
                @JsonProperty("ship_time")
                private Integer shipTime;
                @JsonProperty("can_reserve")
                private Boolean canReserve;
                @JsonProperty("reserve_segments")
                private List<ReserveSegmentsDTO> reserveSegments;

                @NoArgsConstructor
                @Data
                public static class ReserveSegmentsDTO {
                    @JsonProperty("accurate_warehouse_code")
                    private String accurateWarehouseCode;
                    @JsonProperty("start_time")
                    private Long startTime;
                    @JsonProperty("end_time")
                    private Long endTime;
                    @JsonProperty("can_reserve")
                    private Boolean canReserve;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class WarehouseListDTO {
            @JsonProperty("warehouse_code")
            private String warehouseCode;
            @JsonProperty("warehouse_name")
            private String warehouseName;
            @JsonProperty("warehouse_contact")
            private WarehouseContactDTO warehouseContact;

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
        }
    }
}
