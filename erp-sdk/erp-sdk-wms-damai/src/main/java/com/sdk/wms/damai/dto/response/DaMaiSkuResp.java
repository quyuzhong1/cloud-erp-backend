package com.sdk.wms.damai.dto.response;

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
public class DaMaiSkuResp {

    @JsonProperty("status")
    private String status;
    @JsonProperty("statusName")
    private String statusName;
    @JsonProperty("skuCode")
    private String skuCode;
    @JsonProperty("customerSkuCode")
    private String customerSkuCode;
    @JsonProperty("barCode")
    private String barCode;
    @JsonProperty("skuName")
    private String skuName;
    @JsonProperty("skuCost")
    private Integer skuCost;
    @JsonProperty("predictLength")
    private Double predictLength;
    @JsonProperty("predictWidth")
    private Double predictWidth;
    @JsonProperty("predictHeight")
    private Double predictHeight;
    @JsonProperty("predictWeight")
    private Integer predictWeight;
    @JsonProperty("createTime")
    private String createTime;
    @JsonProperty("skuUrl")
    private String skuUrl;
    @JsonProperty("classifyId")
    private String classifyId;
    @JsonProperty("snType")
    private String snType;
    @JsonProperty("offerorName")
    private String offerorName;
    @JsonProperty("emergencyContactNumber")
    private String emergencyContactNumber;
    @JsonProperty("dangerousGoods")
    private String dangerousGoods;
    @JsonProperty("hazardClass")
    private String hazardClass;
    @JsonProperty("idNumber")
    private String idNumber;
    @JsonProperty("isMeasurementMetric")
    private String isMeasurementMetric;
    @JsonProperty("skuProperty")
    private List<String> skuProperty;
    @JsonProperty("skuSnRulesList")
    private Object skuSnRulesList;
    @JsonProperty("skuWhInfo")
    private Object skuWhInfo;
}
