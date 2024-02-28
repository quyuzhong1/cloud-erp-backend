package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class PicturesBean {
    /**
     * id : 913045-MLA40439594053_012020
     * url : https://mla-s2-p.mlstatic.com/913045-MLA40439594053_012020-F.jpg
     */

    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
