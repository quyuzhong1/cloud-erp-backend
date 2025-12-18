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
public class DaMaiCancelFbaOrderRequest {

    @JsonProperty("fbaSoNo")
    private String fbaSoNo;

    @JsonProperty("custRefNo")
    private String custRefNo;
}
