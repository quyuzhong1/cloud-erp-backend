package com.sdk.wms.weishi.dto.response;

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
public class WeiShiOutboundResp {

    @JsonProperty("orderNo")
    private String orderNo;
    @JsonProperty("referNo")
    private String referNo;
    @JsonProperty("extendTrackingNo")
    private String extendTrackingNo;
    @JsonProperty("regionCode")
    private String regionCode;
    @JsonProperty("warehouseCode")
    private String warehouseCode;
    @JsonProperty("platformCode")
    private String platformCode;
    @JsonProperty("orderType")
    private Integer orderType;
    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("channelCode")
    private String channelCode;
    @JsonProperty("deliveryProvider")
    private String deliveryProvider;
    @JsonProperty("trackingNo")
    private String trackingNo;
    @JsonProperty("status")
    private String status;
    @JsonProperty("shipCode")
    private String shipCode;
    @JsonProperty("platformShopCode")
    private String platformShopCode;
    @JsonProperty("labelFile")
    private String labelFile;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("estimateVolume")
    private String estimateVolume;
    @JsonProperty("estimateWeight")
    private String estimateWeight;
    @JsonProperty("actualVolume")
    private String actualVolume;
    @JsonProperty("actualWeight")
    private String actualWeight;
    @JsonProperty("chargeWeight")
    private String chargeWeight;
    @JsonProperty("createDate")
    private String createDate;
    @JsonProperty("auditDate")
    private String auditDate;
    @JsonProperty("estimateCheckOutDate")
    private String estimateCheckOutDate;
    @JsonProperty("pickFinishDate")
    private String pickFinishDate;
    @JsonProperty("packageDate")
    private String packageDate;
    @JsonProperty("checkOutDate")
    private String checkOutDate;
    @JsonProperty("deliveryDate")
    private String deliveryDate;
    @JsonProperty("useSpecifiedMaterial")
    private String useSpecifiedMaterial;
    @JsonProperty("materialCodeList")
    private List<String> materialCodeList;
    @JsonProperty("actualMaterialCodeList")
    private List<String> actualMaterialCodeList;
    @JsonProperty("packageList")
    private List<PackageListDTO> packageList;
    @JsonProperty("recipient")
    private RecipientDTO recipient;

    @NoArgsConstructor
    @Data
    public static class RecipientDTO {
        @JsonProperty("name")
        private String name;
        @JsonProperty("taxno")
        private String taxno;
        @JsonProperty("company")
        private Object company;
        @JsonProperty("postcode")
        private String postcode;
        @JsonProperty("mobile")
        private String mobile;
        @JsonProperty("email")
        private String email;
        @JsonProperty("state")
        private String state;
        @JsonProperty("city")
        private String city;
        @JsonProperty("street")
        private String street;
        @JsonProperty("countrycode")
        private String countrycode;
    }

    @NoArgsConstructor
    @Data
    public static class PackageListDTO {
        @JsonProperty("labelFile")
        private String labelFile;
        @JsonProperty("actualVolume")
        private String actualVolume;
        @JsonProperty("actualWeight")
        private String actualWeight;
        @JsonProperty("checkOutDate")
        private String checkOutDate;
        @JsonProperty("skuList")
        private List<SkuListDTO> skuList;

        @NoArgsConstructor
        @Data
        public static class SkuListDTO {
            @JsonProperty("skuCode")
            private String skuCode;
            @JsonProperty("quantity")
            private String quantity;
        }
    }
}
