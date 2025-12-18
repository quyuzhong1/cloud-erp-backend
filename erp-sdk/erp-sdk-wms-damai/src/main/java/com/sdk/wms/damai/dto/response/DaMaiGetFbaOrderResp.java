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
public class DaMaiGetFbaOrderResp {

    @JsonProperty("fbaSoNo")
    private String fbaSoNo;
    @JsonProperty("custRefNo")
    private String custRefNo;
    @JsonProperty("mainTrackingNo")
    private String mainTrackingNo;
    @JsonProperty("platformFileUrl")
    private String platformFileUrl;
    @JsonProperty("totalSkuQty")
    private Integer totalSkuQty;
    @JsonProperty("totalPackageQty")
    private Integer totalPackageQty;
    @JsonProperty("asnPackageQty")
    private Integer asnPackageQty;
    @JsonProperty("trayQty")
    private Integer trayQty;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("deliverType")
    private String deliverType;
    @JsonProperty("predictionArrivalTime")
    private String predictionArrivalTime;
    @JsonProperty("seType")
    private String seType;
    @JsonProperty("consigneePostalCode")
    private String consigneePostalCode;
    @JsonProperty("consigneeName")
    private String consigneeName;
    @JsonProperty("consigneeCountryCode")
    private String consigneeCountryCode;
    @JsonProperty("consigneeCountryName")
    private String consigneeCountryName;
    @JsonProperty("shippingCountryCode")
    private String shippingCountryCode;
    @JsonProperty("shippingCountryName")
    private String shippingCountryName;
    @JsonProperty("carriersCode")
    private String carriersCode;
    @JsonProperty("endProviderCode")
    private String endProviderCode;
    @JsonProperty("currencyCode")
    private String currencyCode;
    @JsonProperty("shopName")
    private String shopName;
    @JsonProperty("createTime")
    private String createTime;
    @JsonProperty("submitTime")
    private String submitTime;
    @JsonProperty("confirmTime")
    private String confirmTime;
    @JsonProperty("status")
    private String status;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("problemDesc")
    private String problemDesc;
    @JsonProperty("podRemark")
    private String podRemark;
    @JsonProperty("podFileUrl")
    private String podFileUrl;
    @JsonProperty("fbaSoSkuList")
    private List<SoSkuListDTO> fbaSoSkuList;
    @JsonProperty("fbaSoPackageList")
    private List<PackListDTO> fbaSoPackageList;

    @NoArgsConstructor
    @Data
    public static class SoSkuListDTO {
        @JsonProperty("skuCode")
        private String skuCode;
        @JsonProperty("skuQty")
        private Integer skuQty;
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        @JsonProperty("barCode")
        private String barCode;
        @JsonProperty("skuName")
        private String skuName;
        @JsonProperty("custPackageNo")
        private String custPackageNo;
        @JsonProperty("custPackNoPrefix")
        private String custPackNoPrefix;

    }

    @NoArgsConstructor
    @Data
    public static class PackListDTO {
        @JsonProperty("packageNo")
        private String packageNo;
        @JsonProperty("epNo")
        private String epNo;
        @JsonProperty("subEpNo")
        private String subEpNo;
        @JsonProperty("labelFileUrl")
        private String labelFileUrl;
        @JsonProperty("trayNo")
        private String trayNo;
        @JsonProperty("materialCode")
        private String materialCode;
        @JsonProperty("trackingNo")
        private String trackingNo;
        @JsonProperty("length")
        private Double length;
        @JsonProperty("width")
        private Double width;
        @JsonProperty("height")
        private Double height;
        @JsonProperty("weight")
        private Double weight;
        @JsonProperty("volume")
        private Double volume;
        @JsonProperty("grossWeight")
        private Double grossWeight;
        @JsonProperty("billableWeight")
        private Double billableWeight;
    }
}
