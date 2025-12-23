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
public class DaMaiCancelFbaOrderResp {

    @JsonProperty("fbaSoNo")
    private String fbaSoNo;
    @JsonProperty("custRefNo")
    private String custRefNo;
}
