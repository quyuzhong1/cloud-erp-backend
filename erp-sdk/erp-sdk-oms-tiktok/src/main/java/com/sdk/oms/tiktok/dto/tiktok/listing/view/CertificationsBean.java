package com.sdk.oms.tiktok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationsBean {
    /**
     * files : [{"format":"PDF","id":"v09ea0g40000cj91373c77u3mid3g1s0","name":"CERT_X2.PDF","urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"]}]
     * id : 602362
     * images : [{"height":600,"thumb_urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"uri":"tos-maliva-i-o3syd03w52-us/c668cdf70b7f483c94dbe","urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"width":600}]
     * title : SNI Certificate
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("title")
    private String title;
    @JsonProperty("files")
    private List<FilesBean> files;
    @JsonProperty("images")
    private List<ImagesBean> images;
}
