package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName("id")
    private String fid;
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

}
