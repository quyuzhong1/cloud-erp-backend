package com.sdk.wms.damai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiInventoryResp {

    @JsonProperty("skuCode")
    private String skuCode;
    @JsonProperty("skuName")
    private String skuName;
    @JsonProperty("skuNameZh")
    private String skuNameZh;
    @JsonProperty("skuNameEn")
    private String skuNameEn;
    @JsonProperty("skuNameFr")
    private String skuNameFr;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("whName")
    private String whName;
    @JsonProperty("totalQty")
    private Integer totalQty;
    @JsonProperty("onWayQty")
    private Integer onWayQty;
    @JsonProperty("availableQty")
    private Integer availableQty;
    @JsonProperty("unavailableQty")
    private Integer unavailableQty;
    @JsonProperty("returnQty")
    private Integer returnQty;
    @JsonProperty("returnBadQty")
    private Integer returnBadQty;
    @JsonProperty("occupyQty")
    private Integer occupyQty;
    @JsonProperty("waitOutQty")
    private Integer waitOutQty;
    @JsonProperty("shippingQty")
    private Integer shippingQty;
    @JsonProperty("receivingQty")
    private Integer receivingQty;
    @JsonProperty("barCode")
    private String barCode;
    @JsonProperty("customerSkuCode")
    private String customerSkuCode;
}
