package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MunicipalityBean {
    /**
     * id : null
     * name : null
     */

    @JsonProperty("id")
    private Object fid;
    @JsonProperty("name")
    private Object name;

}
