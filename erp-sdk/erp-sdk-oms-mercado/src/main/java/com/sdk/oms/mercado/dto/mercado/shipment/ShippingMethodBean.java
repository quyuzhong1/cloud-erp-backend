package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class ShippingMethodBean {
    /**
     * id : 509450
     * type : standard
     * name : Estándar a domicilio
     * deliver_to : address
     */

    @SerializedName("id")
    private int id;
    @SerializedName("type")
    private String type;
    @SerializedName("name")
    private String name;
    @SerializedName("deliver_to")
    private String deliverTo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(String deliverTo) {
        this.deliverTo = deliverTo;
    }
}
