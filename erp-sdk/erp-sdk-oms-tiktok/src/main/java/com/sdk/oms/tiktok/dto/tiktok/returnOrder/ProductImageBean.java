package com.sdk.oms.tiktok.dto.tiktok.returnOrder;

import com.google.gson.annotations.SerializedName;

public class ProductImageBean {
    /**
     * height : 200
     * url : https://p16-oec-sg.ibyteimg.com/tos-alisg-i-aphluv4xwc-sg/10d1df26601e46fab0683718196bc57d~tplv-aphluv4xwc-origin-jpeg.jpeg?from=4246405447
     * width : 200
     */

    @SerializedName("height")
    private int height;
    @SerializedName("url")
    private String url;
    @SerializedName("width")
    private int width;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
