package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandBean {
    /**
     * id : 7082427311584347905
     * name : brand xxx aaa
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;

}
