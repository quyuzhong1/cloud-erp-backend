package com.sdk.oms.mercado.dto.mercado.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PicturesBean {
    /**
     * id : 750061-CBT73939315659_012024
     * url : http://http2.mlstatic.com/D_750061-CBT73939315659_012024-O.jpg
     * secure_url : https://http2.mlstatic.com/D_750061-CBT73939315659_012024-O.jpg
     * size : 500x385
     * max_size : 1200x924
     * quality :
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("url")
    private String url;
    @JsonProperty("secure_url")
    private String secureUrl;
    @JsonProperty("size")
    private String size;
    @JsonProperty("max_size")
    private String maxSize;
    @JsonProperty("quality")
    private String quality;

}
