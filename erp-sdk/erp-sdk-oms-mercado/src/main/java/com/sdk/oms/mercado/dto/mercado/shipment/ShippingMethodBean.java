package com.sdk.oms.mercado.dto.mercado.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShippingMethodBean {
    /**
     * id : 509450
     * type : standard
     * name : Estándar a domicilio
     * deliver_to : address
     */

    @JsonProperty("id")
    private int fid;
    @JsonProperty("type")
    private String type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("deliver_to")
    private String deliverTo;

}
