package com.sdk.wms.damai.dto.request;

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
public class DaMaiCalculateFeeRequest {

    @JsonProperty("consigneeCountryCode")
    private String consigneeCountryCode;
    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("consigneePostalCode")
    private String consigneePostalCode;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("customerSkuCode")
    private String customerSkuCode;
    @JsonProperty("grossWeight")
    private String grossWeight;
    @JsonProperty("length")
    private String length;
    @JsonProperty("width")
    private String width;
    @JsonProperty("height")
    private String height;
    @JsonProperty("packageQty")
    private String packageQty;
    @JsonProperty("residentialFlag")
    private String residentialFlag;
    @JsonProperty("podFlag")
    private String podFlag;
}
