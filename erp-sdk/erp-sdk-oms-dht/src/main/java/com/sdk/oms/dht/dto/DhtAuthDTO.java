package com.sdk.oms.dht.dto;

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
public class DhtAuthDTO extends BaseResult{


    @JsonProperty("corpAccessToken")
    private String corpAccessToken;

    @JsonProperty("corpId")
    private String corpId;

    @JsonProperty("expiresIn")
    private Integer expiresIn;

    @JsonProperty("traceId")
    private String traceId;
}
