package com.sdk.wms.damai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class DaMaiCreateOrderRequest {

    @JsonProperty("custRefNo")
    private String custRefNo;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("shippingType")
    private String shippingType;
    @JsonProperty("carriersCode")
    private String carriersCode;
    @JsonProperty("orderProperty")
    private List<String> orderProperty;
    @JsonProperty("endProviderCode")
    private String endProviderCode;
    @JsonProperty("trackingNoList")
    private List<String> trackingNoList;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("platformCode")
    private String platformCode;
    @JsonProperty("platformRemark")
    private String platformRemark;
    @JsonProperty("shopRemark")
    private String shopRemark;
    @JsonProperty("shopTel")
    private String shopTel;
    @JsonProperty("consigneeName")
    private String consigneeName;
    @JsonProperty("consigneeTel")
    private String consigneeTel;
    @JsonProperty("consigneeTelExt")
    private String consigneeTelExt;
    @JsonProperty("consigneeCountryCode")
    private String consigneeCountryCode;
    @JsonProperty("consigneeProvince")
    private String consigneeProvince;
    @JsonProperty("consigneeCity")
    private String consigneeCity;
    @JsonProperty("consigneeAddress1")
    private String consigneeAddress1;
    @JsonProperty("consigneeAddress2")
    private String consigneeAddress2;
    @JsonProperty("consigneeAddress3")
    private String consigneeAddress3;
    @JsonProperty("consigneePostalCode")
    private String consigneePostalCode;
    @JsonProperty("consigneeHouseNumber")
    private String consigneeHouseNumber;
    @JsonProperty("consigneeEmail")
    private String consigneeEmail;
    @JsonProperty("specifiedDate")
    private String specifiedDate;
    @JsonProperty("specifiedTime")
    private String specifiedTime;
    @JsonProperty("soSkuList")
    private List<SoSkuListDTO> soSkuList;
    @JsonProperty("soSkuDeclaredList")
    private List<SoSkuDeclaredListDTO> soSkuDeclaredList;
    @JsonProperty("label")
    private LabelDTO label;

    @JsonProperty("attachment")
    private Attachment attachment;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        @JsonProperty("fileType")
        private String fileType;
        @JsonProperty("sourceType")
        private String sourceType;
        @JsonProperty("fileDate")
        private String fileDate;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class LabelDTO {
        @JsonProperty("fileType")
        private String fileType;
        @JsonProperty("sourceType")
        private String sourceType;
        @JsonProperty("fileDate")
        private String fileDate;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class SoSkuListDTO {
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        @JsonProperty("skuQty")
        private Integer skuQty;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class SoSkuDeclaredListDTO {
        @JsonProperty("skuCode")
        private String skuCode;
        @JsonProperty("skuNameEn")
        private String skuNameEn;
        @JsonProperty("declaredValue")
        private String declaredValue;
        @JsonProperty("declaredWeight")
        private String declaredWeight;
        @JsonProperty("skuQty")
        private Integer skuQty;
    }
}
