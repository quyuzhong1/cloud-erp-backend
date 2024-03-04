package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class ValuesBeanX {
    /**
     * id : 2537728
     * name : Ulanzi
     * struct : null
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("struct")
    private Object struct;

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

    public Object getStruct() {
        return struct;
    }

    public void setStruct(Object struct) {
        this.struct = struct;
    }
}
