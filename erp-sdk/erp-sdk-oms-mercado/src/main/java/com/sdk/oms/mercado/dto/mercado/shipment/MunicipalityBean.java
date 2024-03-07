package com.sdk.oms.mercado.dto.mercado.shipment;

import com.google.gson.annotations.SerializedName;

public class MunicipalityBean {
    /**
     * id : null
     * name : null
     */

    @SerializedName("id")
    private Object id;
    @SerializedName("name")
    private Object name;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }
}
