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
public class DaMaiInventoryTransResp {

    @JsonProperty("billNo")
    private String billNo;
    @JsonProperty("billTypeName")
    private String billTypeName;
    @JsonProperty("operationTypeName")
    private String operationTypeName;
    @JsonProperty("skuCode")
    private String skuCode;
    @JsonProperty("skuName")
    private String skuName;
    @JsonProperty("customerSkuCode")
    private String customerSkuCode;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("transQty")
    private Integer transQty;
    @JsonProperty("fmTotalQty")
    private Integer fmTotalQty;
    @JsonProperty("toTotalQty")
    private Integer toTotalQty;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("operationTime")
    private String operationTime;
    private String authId;
}
