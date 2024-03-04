package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;

public class PicturesBean {
    /**
     * id : 750061-CBT73939315659_012024
     * url : http://http2.mlstatic.com/D_750061-CBT73939315659_012024-O.jpg
     * secure_url : https://http2.mlstatic.com/D_750061-CBT73939315659_012024-O.jpg
     * size : 500x385
     * max_size : 1200x924
     * quality :
     */

    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;
    @SerializedName("secure_url")
    private String secureUrl;
    @SerializedName("size")
    private String size;
    @SerializedName("max_size")
    private String maxSize;
    @SerializedName("quality")
    private String quality;

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

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
