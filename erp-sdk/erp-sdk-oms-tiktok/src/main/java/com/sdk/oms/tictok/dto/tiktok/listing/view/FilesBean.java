package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FilesBean {
    /**
     * format : PDF
     * id : v09ea0g40000cj91373c77u3mid3g1s0
     * name : CERT_X2.PDF
     * urls : ["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"]
     */

    @JsonProperty("format")
    private String format;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("urls")
    private List<String> urls;

}
