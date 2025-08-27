package com.sdk.wms.damai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiGetOrderRequest {

    @JsonProperty("soNoList")
    private List<String> soNoList;

    @JsonProperty("custRefNoList")
    private List<String> custRefNoList;
}
