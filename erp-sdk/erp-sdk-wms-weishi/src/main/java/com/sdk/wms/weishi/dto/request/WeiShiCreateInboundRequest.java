package com.sdk.wms.weishi.dto.request;

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
public class WeiShiCreateInboundRequest {

    private String orderNo;
    @JsonProperty("inboundType")
    private String inboundType;
    @JsonProperty("inboundMode")
    private String inboundMode;
    @JsonProperty("transportType")
    private String transportType;
    @JsonProperty("trackingNo")
    private String trackingNo;
    @JsonProperty("destWarehouseCode")
    private String destWarehouseCode;
    @JsonProperty("expectedArriveDate")
    private String expectedArriveDate;
    @JsonProperty("batchNo")
    private String batchNo;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("transportSize")
    private String transportSize;
    @JsonProperty("deliveryVoucherBase64")
    private String deliveryVoucherBase64;
    @JsonProperty("contact")
    private ContactDTO contact;
    @JsonProperty("appointmentPickingStartTime")
    private String appointmentPickingStartTime;
    @JsonProperty("appointmentPickingEndTime")
    private String appointmentPickingEndTime;
    @JsonProperty("inboundBoxList")
    private List<InboundBoxListDTO> inboundBoxList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class ContactDTO {
        @JsonProperty("city")
        private String city;
        @JsonProperty("contactName")
        private String contactName;
        @JsonProperty("countryCode")
        private String countryCode;
        @JsonProperty("phone")
        private String phone;
        @JsonProperty("state")
        private String state;
        @JsonProperty("street")
        private String street;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class InboundBoxListDTO {
        @JsonProperty("fbaProofCode")
        private String fbaProofCode;
        @JsonProperty("boxCode")
        private String boxCode;
        @JsonProperty("boxLength")
        private String boxLength;
        @JsonProperty("boxWidth")
        private String boxWidth;
        @JsonProperty("boxHeight")
        private String boxHeight;
        @JsonProperty("boxWeight")
        private String boxWeight;
        @JsonProperty("sysBoxSeq")
        private Integer sysBoxSeq;
        @JsonProperty("boxRemark")
        private String boxRemark;
        @JsonProperty("pltWarehouseCode")
        private String pltWarehouseCode;
        @JsonProperty("sellerId")
        private String sellerId;
        @JsonProperty("inboundSkuList")
        private List<InboundSkuListDTO> inboundSkuList;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @SuperBuilder
        public static class InboundSkuListDTO {
            @JsonProperty("skuCode")
            private String skuCode;
            @JsonProperty("quantity")
            private Integer quantity;
        }
    }
}
