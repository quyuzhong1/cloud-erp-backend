package com.sdk.oms.tiktok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class FullyListingDTO {

    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("spus")
        private List<SpusDTO> spus;
        @JsonProperty("next_page_token")
        private String nextPageToken;

        @NoArgsConstructor
        @Data
        public static class SpusDTO {
            @JsonProperty("platform_spu_code")
            private String platformSpuCode;
            @JsonProperty("skus")
            private List<SkusDTO> skus;

            @NoArgsConstructor
            @Data
            public static class SkusDTO {
                @JsonProperty("code")
                private String code;
                @JsonProperty("external_sku_code")
                private String externalSkuCode;
                @JsonProperty("status")
                private String status;
                @JsonProperty("update_time")
                private Long updateTime;
                @JsonProperty("reject_reason")
                private String rejectReason;
            }
        }
    }
}
