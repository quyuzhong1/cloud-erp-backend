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
public class DaMaiGetOrderResp {


    @JsonProperty("status")
    private String status;
    @JsonProperty("custRefNo")
    private String custRefNo;
    @JsonProperty("whName")
    private String whName;
    @JsonProperty("orderProperty")
    private List<?> orderProperty;
    @JsonProperty("remark")
    private Object remark;
    @JsonProperty("mainTrackingNo")
    private String mainTrackingNo;
    @JsonProperty("consigneeCpyName")
    private Object consigneeCpyName;
    @JsonProperty("consigneeName")
    private String consigneeName;
    @JsonProperty("consigneeTel")
    private String consigneeTel;
    @JsonProperty("consigneeTelExt")
    private Object consigneeTelExt;
    @JsonProperty("consigneeEmail")
    private String consigneeEmail;
    @JsonProperty("consigneeCountryCode")
    private String consigneeCountryCode;
    @JsonProperty("consigneeProvince")
    private String consigneeProvince;
    @JsonProperty("consigneeCity")
    private String consigneeCity;
    @JsonProperty("consigneeHouseNumber")
    private Object consigneeHouseNumber;
    @JsonProperty("consigneeAddress1")
    private String consigneeAddress1;
    @JsonProperty("consigneeAddress2")
    private Object consigneeAddress2;
    @JsonProperty("consigneeAddress3")
    private Object consigneeAddress3;
    @JsonProperty("consigneePostalCode")
    private String consigneePostalCode;
    @JsonProperty("problemDesc")
    private Object problemDesc;
    @JsonProperty("confirmTime")
    private String confirmTime;
    @JsonProperty("endProviderCode")
    private String endProviderCode;
    @JsonProperty("platformCode")
    private String platformCode;
    @JsonProperty("platformRemark")
    private String platformRemark;
    @JsonProperty("seRemark")
    private Object seRemark;
    @JsonProperty("shopRemark")
    private String shopRemark;
    @JsonProperty("shopTel")
    private String shopTel;
    @JsonProperty("specifiedDate")
    private Object specifiedDate;
    @JsonProperty("specifiedTime")
    private Object specifiedTime;
    @JsonProperty("labelFileUrl")
    private String labelFileUrl;
    @JsonProperty("soSkuList")
    private List<SoSkuListDTO> soSkuList;
    @JsonProperty("packList")
    private List<PackListDTO> packList;
    @JsonProperty("snList")
    private List<?> snList;
    @JsonProperty("soNo")
    private String soNo;
    @JsonProperty("totalPackageQty")
    private Integer totalPackageQty;
    @JsonProperty("totalGrossWeight")
    private Integer totalGrossWeight;
    @JsonProperty("totalVolume")
    private Double totalVolume;
    @JsonProperty("totalDimensionalWeight")
    private Double totalDimensionalWeight;
    @JsonProperty("totalBillableWeight")
    private Integer totalBillableWeight;
    @JsonProperty("createTime")
    private String createTime;

    @NoArgsConstructor
    @Data
    public static class SoSkuListDTO {
        @JsonProperty("skuCode")
        private String skuCode;
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        @JsonProperty("skuQty")
        private Integer skuQty;
    }

    @NoArgsConstructor
    @Data
    public static class PackListDTO {
        @JsonProperty("packageNo")
        private String packageNo;
        @JsonProperty("labelFileUrl")
        private String labelFileUrl;
        @JsonProperty("trackingNo")
        private String trackingNo;
        @JsonProperty("length")
        private Double length;
        @JsonProperty("width")
        private Double width;
        @JsonProperty("height")
        private Double height;
        @JsonProperty("weight")
        private Integer weight;
        @JsonProperty("billableWeight")
        private Integer billableWeight;
        @JsonProperty("volume")
        private Double volume;
        @JsonProperty("dimensionalWeight")
        private Double dimensionalWeight;
        @JsonProperty("materialCode")
        private Object materialCode;
    }
}
