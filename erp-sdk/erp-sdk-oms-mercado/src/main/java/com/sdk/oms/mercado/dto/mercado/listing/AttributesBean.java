package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class AttributesBean {
    /**
     * id : BRAND
     * name : Brand
     * value_id : 59387
     * value_name : Xiaomi
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("value_id")
    private String valueId;
    @SerializedName("value_name")
    private String valueName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
