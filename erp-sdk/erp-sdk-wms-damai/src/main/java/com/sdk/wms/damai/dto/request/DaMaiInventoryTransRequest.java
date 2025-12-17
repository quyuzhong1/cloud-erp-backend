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
public class DaMaiInventoryTransRequest {

    @JsonProperty("customerSkuCodeList")
    private List<String> customerSkuCodeList;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("startOperationTime")
    private String startOperationTime;
    @JsonProperty("endOperationTime")
    private String endOperationTime;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("page")
    private Integer page;
}
