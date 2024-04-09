package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryChainsBean {
    /**
     * id : 853000
     * is_leaf : true
     * local_name : Botol & Stoples Penyimpanan
     * parent_id : 851848
     */

    @JsonProperty("id")
    private String fid;
    @JsonProperty("is_leaf")
    private boolean isLeaf;
    @JsonProperty("local_name")
    private String localName;
    @JsonProperty("parent_id")
    private String parentId;

}
