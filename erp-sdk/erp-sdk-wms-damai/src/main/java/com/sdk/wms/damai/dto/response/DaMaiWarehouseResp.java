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
public class DaMaiWarehouseResp {

    @JsonProperty("id")
    private String id;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("whName")
    private String whName;
    @JsonProperty("countryCode")
    private String countryCode;
    @JsonProperty("currencyCode")
    private String currencyCode;
    @JsonProperty("weightUnit")
    private String weightUnit;
    @JsonProperty("lengthUnit")
    private String lengthUnit;
}
