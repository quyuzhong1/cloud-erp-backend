package com.sdk.oms.tictok.dto.tiktok.listing.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkusBean {
    /**
     * external_sku_id : 1729592969712207234
     * id : 10001
     * identifier_code : {"code":"10000000000010","type":"GTIN"}
     * inventory : [{"quantity":999,"warehouse_id":"6966568648651605766"}]
     * price : {"currency":"USD","sale_price":"117.5","tax_exclusive_price":"110"}
     * sales_attributes : [{"id":"100000","name":"Color","sku_img":{"height":100,"thumb_urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"uri":"tos-maliva-i-o3syd03w52-us/c668cdf70b7f483c94dbe","urls":["https://p16-oec-va.ibyteimg.com/tos-maliva-i-o3syd03w52-us/6c8519a3663a4d728c4e3c131dc914b4~tplv-o3syd03w52-resize-jpeg:300:300.jpeg?from=522366036"],"width":100},"value_id":"100000","value_name":"Red"}]
     * seller_sku : sku name
     */

    @JsonProperty("external_sku_id")
    private String externalSkuId;
    @JsonProperty("id")
    private String fid;
    @JsonProperty("identifier_code")
    private IdentifierCodeBean identifierCode;
    @JsonProperty("price")
    private PriceBean price;
    @JsonProperty("seller_sku")
    private String sellerSku;
    @JsonProperty("inventory")
    private List<InventoryBean> inventory;
    @JsonProperty("sales_attributes")
    private List<SalesAttributesBean> salesAttributes;

}
