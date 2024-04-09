package com.sdk.oms.tictok.dto.tiktok.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistrictInfoBean {
    /**
     * address_level : L0
     * address_level_name : Country
     * address_name : United Kingdom
     */

    @JsonProperty("address_level")
    private String addressLevel;
    @JsonProperty("address_level_name")
    private String addressLevelName;
    @JsonProperty("address_name")
    private String addressName;

}
