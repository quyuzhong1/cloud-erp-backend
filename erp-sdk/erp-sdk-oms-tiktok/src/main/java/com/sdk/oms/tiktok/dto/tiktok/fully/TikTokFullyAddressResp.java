package com.sdk.oms.tiktok.dto.tiktok.fully;

import com.fasterxml.jackson.annotation.JsonProperty;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TikTokFullyAddressResp {

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
        @JsonProperty("addresses")
        private List<AddressesDTO> addresses;

        @NoArgsConstructor
        @Data
        public static class AddressesDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("contact_name")
            private String contactName;
            @JsonProperty("phone_number")
            private String phoneNumber;
            @JsonProperty("full_address")
            private String fullAddress;
            @JsonProperty("detail")
            private DetailDTO detail;

            @NoArgsConstructor
            @Data
            public static class DetailDTO {
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
                @JsonProperty("building")
                private String building;
            }
        }
    }
}
