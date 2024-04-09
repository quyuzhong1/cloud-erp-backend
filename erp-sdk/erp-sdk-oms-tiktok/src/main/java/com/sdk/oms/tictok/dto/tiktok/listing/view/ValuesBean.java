package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValuesBean {
    /**
     * id : 1001533
     * name : Birthday
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
}
