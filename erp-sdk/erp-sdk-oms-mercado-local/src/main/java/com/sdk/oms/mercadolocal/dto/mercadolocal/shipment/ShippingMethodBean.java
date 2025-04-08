package com.sdk.oms.mercadolocal.dto.mercadolocal.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingMethodBean {
    /**
     * id : 509450
     * type : standard
     * name : Estándar a domicilio
     * deliver_to : address
     */

    @JsonProperty("id")
    private long fid;
    @JsonProperty("type")
    private String type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("deliver_to")
    private String deliverTo;

}
