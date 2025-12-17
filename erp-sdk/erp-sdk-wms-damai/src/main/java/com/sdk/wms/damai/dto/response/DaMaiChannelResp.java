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
public class DaMaiChannelResp {


    @JsonProperty("carriersCode")
    private String carriersCode;
    @JsonProperty("carriersName")
    private String carriersName;
    @JsonProperty("countryCode")
    private String countryCode;
}
