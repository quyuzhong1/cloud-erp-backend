package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoBean {
    /**
     * cover_url : https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036
     * format : MP4
     * height : 480
     * id : v09ea0g40000cj91373c77u3mid3g1s0
     * size : 1000
     * url : https://v16m-default.akamaized.net/bbae7099581b26cd340beaa7821b2d8c/64de6020/video/tos/alisg/tos-alisg-v-f466fc-sg/oMne9QuzIBN3fIDN7bFCCMbBKuGigg12ghDC8k/?a=0&ch=0&cr=0&dr=0&er=0&lr=default&cd=0%7C0%7C0%7C0&br=2212&bt=1106&cs=0&ds=3&ft=dl9~j-Inz7TKnfsfiyq8Z&mime_type=video_mp4&qs=13&rc=anR4Ojk6ZmYzbTMzODRmNEBpanR4Ojk6ZmYzbTMzODRmNEBsYWFwcjRva2NgLS1kLy1zYSNsYWFwcjRva2NgLS1kLy1zcw%3D%3D&l=202308171159498F7B108584E58B010932&btag=e00048000
     * width : 1280
     */

    @JsonProperty("cover_url")
    private String coverUrl;
    @JsonProperty("format")
    private String format;
    @JsonProperty("height")
    private int height;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("size")
    private int size;
    @JsonProperty("url")
    private String url;
    @JsonProperty("width")
    private int width;

}
