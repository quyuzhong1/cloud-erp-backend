package com.sdk.oms.tiktok.dto.tiktok.listing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListingDTO {


    /**
     * code : 0
     * data : {"next_page_token":"","products":[{"create_time":1712541502,"id":"1729591506865653481","sales_regions":["ID"],"skus":[{"id":"1729591423391532777","inventory":[{"quantity":11111,"warehouse_id":"7354364871201720069"}],"price":{"currency":"IDR","tax_exclusive_price":"11111111"},"seller_sku":"1111"}],"status":"ACTIVATE","title":"Ulanzi St-06 Trípode Soporte Flexible Para Cámara Y Teléfono","update_time":1712542016}],"total_count":1}
     * message : Success
     * request_id : 202404080251285A2A9C2558C89B0DD415
     */

    @JsonProperty("code")
    private int code;
    @JsonProperty("data")
    private DataBean data;
    @JsonProperty("message")
    private String message;
    @JsonProperty("request_id")
    private String requestId;

}
