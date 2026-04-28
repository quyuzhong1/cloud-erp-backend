package com.sdk.wms.jifeng.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
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
public class JiFengCreateB2BOutboundRequest {

    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "warehouse")
    private String warehouse;
    @JSONField(name = "destination")
    private Integer destination;
    @JSONField(name = "dispatchType")
    private Integer dispatchType;
    @JSONField(name = "logisticType")
    private Integer logisticType;
    @JSONField(name = "trackingNo")
    private String trackingNo;
    @JSONField(name = "expectTime")
    private String expectTime;
    @JSONField(name = "referenceNo")
    private String referenceNo;
    @JSONField(name = "fbaShipmentId")
    private String fbaShipmentId;
    @JSONField(name = "outboundType")
    private Integer outboundType;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "fullInboundNo")
    private String fullInboundNo;
    @JSONField(name = "pickupCode")
    private String pickupCode;
    @JSONField(name = "deliveryCode")
    private String deliveryCode;
    @JSONField(name = "addressVo")
    private AddressVoDTO addressVo;
    @JSONField(name = "boxList")
    private List<BoxListDTO> boxList;
    @JSONField(name = "skuInfoList")
    private List<SkuInfoListDTO> skuInfoList;
    @JSONField(name = "fileInfo")
    private FileInfoDTO fileInfo;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class AddressVoDTO {
        @JSONField(name = "buyerName")
        private String buyerName;
        @JSONField(name = "buyerPhone")
        private String buyerPhone;
        @JSONField(name = "recipientCountry")
        private String recipientCountry;
        @JSONField(name = "recipientProvince")
        private String recipientProvince;
        @JSONField(name = "recipientCity")
        private String recipientCity;
        @JSONField(name = "recipientArea")
        private String recipientArea;
        @JSONField(name = "recipientAddress")
        private String recipientAddress;
        @JSONField(name = "recipientAddress2")
        private String recipientAddress2;
        @JSONField(name = "zipCode")
        private String zipCode;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class FileInfoDTO {
        @JSONField(name = "labelJson")
        private String labelJson;
        @JSONField(name = "bolJson")
        private String bolJson;
        @JSONField(name = "boxJson")
        private String boxJson;
        @JSONField(name = "otherJson")
        private String otherJson;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class BoxListDTO {
        @JSONField(name = "boxNo")
        private String boxNo;
        @JSONField(name = "skuInfoList")
        private List<SkuInfoListDTO> skuInfoList;

        @NoArgsConstructor
        @Data
        public static class SkuInfoListDTO {
            @JSONField(name = "sku")
            private String sku;
            @JSONField(name = "count")
            private Integer count;
            @JSONField(name = "changeLabel")
            private Integer changeLabel;
            @JSONField(name = "pickUpCount")
            private Integer pickUpCount;
            @JSONField(name = "newLabelJson")
            private String newLabelJson;
        }
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class SkuInfoListDTO {
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "count")
        private Integer count;
        @JSONField(name = "changeLabel")
        private Integer changeLabel;
        @JSONField(name = "pickUpCount")
        private Integer pickUpCount;
        @JSONField(name = "newLabelJson")
        private String newLabelJson;
    }
}
