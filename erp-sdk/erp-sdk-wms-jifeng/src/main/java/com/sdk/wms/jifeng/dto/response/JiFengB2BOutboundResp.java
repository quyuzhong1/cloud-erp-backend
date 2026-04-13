package com.sdk.wms.jifeng.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
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
public class JiFengB2BOutboundResp {

    @JSONField(name = "outboundNo")
    private String outboundNo;
    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "warehouse")
    private String warehouse;
    @JSONField(name = "status")
    private Integer status;
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
    @JSONField(name = "shippedTime")
    private String shippedTime;
    @JSONField(name = "referenceNo")
    private String referenceNo;
    @JSONField(name = "fbaShipmentId")
    private String fbaShipmentId;
    @JSONField(name = "changeLabel")
    private Integer changeLabel;
    @JSONField(name = "outboundType")
    private Integer outboundType;
    @JSONField(name = "packBoxStatus")
    private Integer packBoxStatus;
    @JSONField(name = "fullInboundNo")
    private String fullInboundNo;
    @JSONField(name = "pickupCode")
    private String pickupCode;
    @JSONField(name = "deliveryCode")
    private String deliveryCode;
    @JSONField(name = "errorMsg")
    private String errorMsg;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "boxVoList")
    private List<BoxVoListDTO> boxVoList;
    @JSONField(name = "skuInfoList")
    private List<SkuInfoListDTO> skuInfoList;
    @JSONField(name = "addressVo")
    private AddressVoDTO addressVo;
    @JSONField(name = "fileInfo")
    private FileInfoDTO fileInfo;

    @NoArgsConstructor
    @Data
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
    public static class BoxVoListDTO {
        @JSONField(name = "boxNo")
        private String boxNo;
        @JSONField(name = "customBoxNo")
        private String customBoxNo;
        @JSONField(name = "count")
        private Integer count;
        @JSONField(name = "length")
        private Integer length;
        @JSONField(name = "width")
        private Integer width;
        @JSONField(name = "height")
        private Integer height;
        @JSONField(name = "volume")
        private Integer volume;
        @JSONField(name = "weight")
        private Integer weight;
    }

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
