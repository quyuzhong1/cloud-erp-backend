package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageBean {
    /**
     * height : 600
     * thumb_urls : ["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"]
     * uri : tos-maliva-i-o3syd03w52-us/c668cdf70b7f483c94dbe
     * urls : ["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"]
     * width : 600
     */

    @JsonProperty("height")
    private int height;
    @JsonProperty("uri")
    private String uri;
    @JsonProperty("width")
    private int width;
    @JsonProperty("thumb_urls")
    private List<String> thumbUrls;
    @JsonProperty("urls")
    private List<String> urls;

}
