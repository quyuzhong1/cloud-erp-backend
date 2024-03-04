package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class ValuesBean {
    /**
     * id : null
     * name : 1 months
     * struct : {"number":1,"unit":"months"}
     */

    @SerializedName("id")
    private Object id;
    @SerializedName("name")
    private String name;
    @SerializedName("struct")
    private StructBean struct;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StructBean getStruct() {
        return struct;
    }

    public void setStruct(StructBean struct) {
        this.struct = struct;
    }
}
