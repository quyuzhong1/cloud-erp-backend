package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SalesAttributesBean {
    /**
     * id : 100000
     * name : Color
     * sku_img : {"height":100,"thumb_urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"uri":"tos-maliva-i-o3syd03w52-us/c668cdf70b7f483c94dbe","urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"width":100}
     * value_id : 100000
     * value_name : Red
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("sku_img")
    private SkuImgBean skuImg;
    @JsonProperty("value_id")
    private String valueId;
    @JsonProperty("value_name")
    private String valueName;

}
