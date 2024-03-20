package com.sdk.oms.mercado.dto.mercado.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemsBean {
    /**
     * id : MLM2802963514
     */

    @JsonProperty("id")
    private String fid;

}
