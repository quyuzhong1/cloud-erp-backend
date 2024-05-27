package com.sdk.tms.tiktok.channel.provider;

import com.google.gson.annotations.SerializedName;

public class ShippingProvidersBean {
    /**
     * id : 6617675021119438849
     * name : TT Virtual JNT express
     */

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;

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
}
