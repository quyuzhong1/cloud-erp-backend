package com.sdk.wms.iml.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImlCalculateFeeReq {

    @JSONField(name = "platformCustomerCode")
    private String platformCustomerCode;
    @JSONField(name = "orderType")
    private String orderType;
    @JSONField(name = "orderNo")
    private String orderNo;
    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "outboundMode")
    private String outboundMode;
    @JSONField(name = "bizType")
    private String bizType;
    @JSONField(name = "transportProductCode")
    private String transportProductCode;
    @JSONField(name = "selfDelivery")
    private Boolean selfDelivery;
    @JSONField(name = "insuranceProduct")
    private Boolean insuranceProduct;
    @JSONField(name = "orderBoxes")
    private List<OrderBoxesDTO> orderBoxes;
    @JSONField(name = "skus")
    private List<SkusDTO> skus;
    @JSONField(name = "address")
    private AddressDTO address;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddressDTO {
        @JSONField(name = "country")
        private String country;
        @JSONField(name = "province")
        private String province;
        @JSONField(name = "city")
        private String city;
        @JSONField(name = "county")
        private String county;
        @JSONField(name = "address")
        private String address;
        @JSONField(name = "postcode")
        private String postcode;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderBoxesDTO {
        @JSONField(name = "packagingType")
        private String packagingType;
        @JSONField(name = "packagingNo")
        private String packagingNo;
        @JSONField(name = "length")
        private BigDecimal length;
        @JSONField(name = "width")
        private BigDecimal width;
        @JSONField(name = "height")
        private BigDecimal height;
        @JSONField(name = "volume")
        private BigDecimal volume;
        @JSONField(name = "weight")
        private BigDecimal weight;
        @JSONField(name = "rawWeight")
        private BigDecimal rawWeight;
        @JSONField(name = "matchWeight")
        private BigDecimal matchWeight;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SkusDTO {
        @JSONField(name = "skuCode")
        private String skuCode;
        @JSONField(name = "skuBarcode")
        private String skuBarcode;
        @JSONField(name = "skuName")
        private String skuName;
        @JSONField(name = "skuCategory")
        private List<Integer> skuCategory;
        @JSONField(name = "length")
        private Integer length;
        @JSONField(name = "width")
        private Integer width;
        @JSONField(name = "height")
        private Integer height;
        @JSONField(name = "weight")
        private Integer weight;
        @JSONField(name = "qty")
        private Integer qty;
        @JSONField(name = "insuranceAmount")
        private Integer insuranceAmount;
        @JSONField(name = "insuranceCurrency")
        private String insuranceCurrency;
    }
}
