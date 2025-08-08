package com.sdk.wms.weishi.dto.response;

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
public class WeiShiChannelResp {

    @JsonProperty("warehouseCode")
    private String warehouseCode;
    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("productShortCode")
    private String productShortCode;
    @JsonProperty("cnName")
    private String cnName;
    @JsonProperty("enName")
    private String enName;
    @JsonProperty("isMultiPackage")
    private Integer isMultiPackage;
}
