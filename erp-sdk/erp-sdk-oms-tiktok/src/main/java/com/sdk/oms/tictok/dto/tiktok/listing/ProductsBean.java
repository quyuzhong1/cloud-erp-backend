package com.sdk.oms.tictok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductsBean {
    /**
     * create_time : 1712541502
     * id : 1729591506865653481
     * sales_regions : ["ID"]
     * skus : [{"id":"1729591423391532777","inventory":[{"quantity":11111,"warehouse_id":"7354364871201720069"}],"price":{"currency":"IDR","tax_exclusive_price":"11111111"},"seller_sku":"1111"}]
     * status : ACTIVATE
     * title : Ulanzi St-06 Trípode Soporte Flexible Para Cámara Y Teléfono
     * update_time : 1712542016
     */

    @JsonProperty("create_time")
    private int createTime;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("status")
    private String status;
    @JsonProperty("title")
    private String title;
    @JsonProperty("update_time")
    private int updateTime;
    @JsonProperty("sales_regions")
    private List<String> salesRegions;
    @JsonProperty("skus")
    private List<SkusBean> skus;

}
