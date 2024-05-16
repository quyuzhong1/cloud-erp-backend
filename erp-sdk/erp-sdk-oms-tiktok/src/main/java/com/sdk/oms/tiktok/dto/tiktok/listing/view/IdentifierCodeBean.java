package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentifierCodeBean {
    /**
     * code : 10000000000010
     * type : GTIN
     */

    @JsonProperty("code")
    private String code;
    @JsonProperty("type")
    private String type;

}
