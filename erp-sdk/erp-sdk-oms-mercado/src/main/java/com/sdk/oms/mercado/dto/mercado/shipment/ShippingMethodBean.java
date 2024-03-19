package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ShippingMethodBean {
    /**
     * id : 509450
     * type : standard
     * name : Estándar a domicilio
     * deliver_to : address
     */

    @SerializedName("id")
    private int fid;
    @SerializedName("type")
    private String type;
    @SerializedName("name")
    private String name;
    @SerializedName("deliver_to")
    private String deliverTo;

}
