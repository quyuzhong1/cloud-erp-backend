package com.sdk.wms.weishi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jnr.ffi.annotations.In;
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
public class WeiShiCreateOutboundRequest {

    @JsonProperty("referNo")
    private String referNo;
    @JsonProperty("warehouseCode")
    private String warehouseCode;
    @JsonProperty("platformCode")
    private String platformCode;
    @JsonProperty("orderType")
    private Integer orderType;
    @JsonProperty("hasAddedServices")
    private String hasAddedServices;
    @JsonProperty("addedServicesDetail")
    private AddedServicesDetailDTO addedServicesDetail;
    @JsonProperty("pickupTime")
    private String pickupTime;
    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("labelFile")
    private Object labelFile;
    @JsonProperty("useSpecifiedMaterial")
    private String useSpecifiedMaterial;
    @JsonProperty("materialCode")
    private String materialCode;
    @JsonProperty("skuList")
    private List<SkuListDTO> skuList;
    @JsonProperty("recipient")
    private RecipientDTO recipient;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class AddedServicesDetailDTO {
        @JsonProperty("addedServicesAmount")
        private Double addedServicesAmount;
        @JsonProperty("addedServicesCurrency")
        private String addedServicesCurrency;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class SkuListDTO {
        @JsonProperty("skuCode")
        private String skuCode;
        @JsonProperty("quantity")
        private Integer quantity;
    }
}
