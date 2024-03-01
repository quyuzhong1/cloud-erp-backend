package com.sdk.oms.mercado.dto.mercado.listing;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class PicturesBean {
    /**
     * id : 913045-MLA40439594053_012020
     * url : https://mla-s2-p.mlstatic.com/913045-MLA40439594053_012020-F.jpg
     */

    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;

}
