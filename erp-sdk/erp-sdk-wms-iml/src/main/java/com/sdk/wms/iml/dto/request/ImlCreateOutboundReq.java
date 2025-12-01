package com.sdk.wms.iml.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImlCreateOutboundReq {

    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "platformOrderCode")
    private String platformOrderCode;
    @JSONField(name = "ecPlatform")
    private String ecPlatform;
    @JSONField(name = "ecPlatformOrderNo")
    private String ecPlatformOrderNo;
    @JSONField(name = "platformCustomerCode")
    private String platformCustomerCode;
    @JSONField(name = "logisticsCode")
    private String logisticsCode;
    @JSONField(name = "additionalService")
    private List<String> additionalService;
    @JSONField(name = "bizType")
    private String bizType;
    @JSONField(name = "orderType")
    private String orderType;
    @JSONField(name = "warehouseCode")
    private String warehouseCode;
    @JSONField(name = "trackNumber")
    private String trackNumber;
    @JSONField(name = "buyerCountry")
    private String buyerCountry;
    @JSONField(name = "buyerProvince")
    private String buyerProvince;
    @JSONField(name = "buyerCity")
    private String buyerCity;
    @JSONField(name = "buyerAddress")
    private String buyerAddress;
    @JSONField(name = "buyerAddress2")
    private String buyerAddress2;
    @JSONField(name = "buyerName")
    private String buyerName;
    @JSONField(name = "buyerPhone")
    private String buyerPhone;
    @JSONField(name = "buyerEmail")
    private String buyerEmail;
    @JSONField(name = "buyerPostcode")
    private String buyerPostcode;
    @JSONField(name = "buyerHouseNumber")
    private String buyerHouseNumber;
    @JSONField(name = "buyerCompanyName")
    private String buyerCompanyName;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "ullageFlag")
    private String ullageFlag;
    @JSONField(name = "insuranceService")
    private String insuranceService;
    @JSONField(name = "shopId")
    private String shopId;
    @JSONField(name = "shopName")
    private String shopName;
    @JSONField(name = "extraAttributes")
    private String extraAttributes;
    @JSONField(name = "detailList")
    private List<DetailListDTO> detailList;
    @JSONField(name = "outboundMode")
    private String outboundMode;
    @JSONField(name = "takeDeliveryPerson")
    private String takeDeliveryPerson;
    @JSONField(name = "takeDeliveryPersonPhone")
    private String takeDeliveryPersonPhone;
    @JSONField(name = "requireDeliveryTime")
    private Integer requireDeliveryTime;
    @JSONField(name = "logisticsTransferName")
    private String logisticsTransferName;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DetailListDTO {
        @JSONField(name = "skuCode")
        private String skuCode;
        @JSONField(name = "skuBarcode")
        private String skuBarcode;
        @JSONField(name = "declaredAmount")
        private Integer declaredAmount;
        @JSONField(name = "skuCount")
        private Integer skuCount;
        @JSONField(name = "insuranceAmount")
        private Integer insuranceAmount;
        @JSONField(name = "platformDetailId")
        private String platformDetailId;
        @JSONField(name = "snCode")
        private String snCode;
        @JSONField(name = "ownerUserId")
        private String ownerUserId;
    }
}
