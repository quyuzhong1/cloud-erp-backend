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
public class ImlCreateInboundReq {
    @JSONField(name = "code")
    private String code;

    @JSONField(name = "needCustomerAudit")
    private String needCustomerAudit;
    @JSONField(name = "platformOrderNo")
    private String platformOrderNo;
    @JSONField(name = "platformCustomerCode")
    private String platformCustomerCode;
    @JSONField(name = "bizType")
    private String bizType;
    @JSONField(name = "destWarehouseCode")
    private String destWarehouseCode;
    @JSONField(name = "customsService")
    private String customsService;
    @JSONField(name = "customsType")
    private String customsType;
    @JSONField(name = "inboundType")
    private String inboundType;
    @JSONField(name = "logisticsCode")
    private String logisticsCode;
    @JSONField(name = "expectedDate")
    private Long expectedDate;
    @JSONField(name = "description")
    private String description;
    @JSONField(name = "dataSources")
    private String dataSources;
    @JSONField(name = "transit")
    private TransitDTO transit;
    @JSONField(name = "direct")
    private DirectDTO direct;
    @JSONField(name = "boxs")
    private List<BoxsDTO> boxs;
    @JSONField(name = "attachments")
    private List<AttachmentsDTO> attachments;
    @JSONField(name = "additionalService")
    private List<String> additionalService;

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static class TransitDTO {
        @JSONField(name = "transitWarehouseCode")
        private String transitWarehouseCode;
        @JSONField(name = "deliveryType")
        private String deliveryType;
        @JSONField(name = "collectAddress")
        private CollectAddressDTO collectAddress;
        @JSONField(name = "deliveryVehicle")
        private DeliveryVehicleDTO deliveryVehicle;
        @JSONField(name = "deliveryLogistics")
        private DeliveryLogisticsDTO deliveryLogistics;

        @NoArgsConstructor
        @Data
        @Builder
        @AllArgsConstructor
        public static class CollectAddressDTO {
            @JSONField(name = "country")
            private String country;
            @JSONField(name = "countryCode")
            private String countryCode;
            @JSONField(name = "postcode")
            private String postcode;
            @JSONField(name = "province")
            private String province;
            @JSONField(name = "city")
            private String city;
            @JSONField(name = "county")
            private String county;
            @JSONField(name = "street")
            private String street;
            @JSONField(name = "contacter")
            private String contacter;
            @JSONField(name = "contactPhone")
            private String contactPhone;
        }

        @NoArgsConstructor
        @Data
        @Builder
        @AllArgsConstructor
        public static class DeliveryVehicleDTO {
            @JSONField(name = "vehicleNo")
            private String vehicleNo;
            @JSONField(name = "driverName")
            private String driverName;
            @JSONField(name = "driverPhone")
            private String driverPhone;
        }

        @NoArgsConstructor
        @Data
        @Builder
        @AllArgsConstructor
        public static class DeliveryLogisticsDTO {
            @JSONField(name = "deliveryCompany")
            private String deliveryCompany;
            @JSONField(name = "deliveryTrackingNumber")
            private String deliveryTrackingNumber;
            @JSONField(name = "deliverySender")
            private String deliverySender;
            @JSONField(name = "deliveryAddress")
            private String deliveryAddress;
        }
    }

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static class DirectDTO {
        @JSONField(name = "trackingNumber")
        private String trackingNumber;
    }

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static class BoxsDTO {
        @JSONField(name = "palletNo")
        private String palletNo;
        @JSONField(name = "boxNo")
        private String boxNo;
        @JSONField(name = "boxLength")
        private BigDecimal boxLength;
        @JSONField(name = "boxWidth")
        private BigDecimal boxWidth;
        @JSONField(name = "boxHeight")
        private BigDecimal boxHeight;
        @JSONField(name = "boxWeight")
        private BigDecimal boxWeight;
        @JSONField(name = "boxDetails")
        private List<BoxDetailsDTO> boxDetails;

        @NoArgsConstructor
        @Data
        @Builder
        @AllArgsConstructor
        public static class BoxDetailsDTO {
            @JSONField(name = "skuCode")
            private String skuCode;
            @JSONField(name = "skuBarcode")
            private String skuBarcode;
            @JSONField(name = "snCode")
            private String snCode;
            @JSONField(name = "quantity")
            private Integer quantity;
            @JSONField(name = "packageType")
            private String packageType;
            @JSONField(name = "isInsurance")
            private String isInsurance;
            @JSONField(name = "insuredAmount")
            private Integer insuredAmount;
            @JSONField(name = "platformItemId")
            private String platformItemId;
            @JSONField(name = "platformOrderItemId")
            private String platformOrderItemId;
        }
    }

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static class AttachmentsDTO {
        @JSONField(name = "fileName")
        private String fileName;
        @JSONField(name = "fileType")
        private String fileType;
        @JSONField(name = "fileData")
        private String fileData;
        @JSONField(name = "attachedType")
        private String attachedType;
    }
}
