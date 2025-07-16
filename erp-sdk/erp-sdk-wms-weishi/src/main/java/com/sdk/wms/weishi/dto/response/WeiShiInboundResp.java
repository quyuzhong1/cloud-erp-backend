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
public class WeiShiInboundResp {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("rows")
    private List<RowsDTO> rows;

    @NoArgsConstructor
    @Data
    public static class RowsDTO {
        @JsonProperty("orderNo")
        private String orderNo;
        @JsonProperty("inboundType")
        private String inboundType;
        @JsonProperty("inboundMode")
        private String inboundMode;
        @JsonProperty("trackingNo")
        private String trackingNo;
        @JsonProperty("batchNo")
        private String batchNo;
        @JsonProperty("transportType")
        private String transportType;
        @JsonProperty("transportSize")
        private String transportSize;
        @JsonProperty("destWarehouseCode")
        private String destWarehouseCode;
        @JsonProperty("warehouseCode")
        private String warehouseCode;
        @JsonProperty("status")
        private String status;
        @JsonProperty("remark")
        private String remark;
        @JsonProperty("forecastBoxQty")
        private String forecastBoxQty;
        @JsonProperty("forecastSkuKindsQty")
        private String forecastSkuKindsQty;
        @JsonProperty("forecastSkuQty")
        private String forecastSkuQty;
        @JsonProperty("receiptBoxQty")
        private String receiptBoxQty;
        @JsonProperty("receiptSkuKindsQty")
        private String receiptSkuKindsQty;
        @JsonProperty("receiptSkuQty")
        private String receiptSkuQty;
        @JsonProperty("receiptQty")
        private String receiptQty;
        @JsonProperty("putawayBoxQty")
        private Integer putawayBoxQty;
        @JsonProperty("putawaySkuQty")
        private Integer putawaySkuQty;
        @JsonProperty("createTime")
        private String createTime;
        @JsonProperty("expectedArriveDate")
        private String expectedArriveDate;
        @JsonProperty("localReceiptFirstTime")
        private String localReceiptFirstTime;
        @JsonProperty("receiptFirstTime")
        private String receiptFirstTime;
        @JsonProperty("localReceiptLastTime")
        private String localReceiptLastTime;
        @JsonProperty("receiptLastTime")
        private String receiptLastTime;
        @JsonProperty("localFinishPutawayTime")
        private String localFinishPutawayTime;
        @JsonProperty("finishPutawayTime")
        private String finishPutawayTime;
        @JsonProperty("appointmentPickingStartTime")
        private String appointmentPickingStartTime;
        @JsonProperty("appointmentPickingEndTime")
        private String appointmentPickingEndTime;
        @JsonProperty("deliveryVoucherUrl")
        private String deliveryVoucherUrl;
        @JsonProperty("contacts")
        private ContactsDTO contacts;
        @JsonProperty("inboundBoxList")
        private List<InboundBoxListDTO> inboundBoxList;

        @NoArgsConstructor
        @Data
        public static class ContactsDTO {
            @JsonProperty("contactName")
            private String contactName;
            @JsonProperty("phone")
            private String phone;
            @JsonProperty("countryCode")
            private String countryCode;
            @JsonProperty("state")
            private String state;
            @JsonProperty("city")
            private String city;
            @JsonProperty("street")
            private String street;
        }

        @NoArgsConstructor
        @Data
        public static class InboundBoxListDTO {
            @JsonProperty("fbaProofCode")
            private String fbaProofCode;
            @JsonProperty("boxCode")
            private String boxCode;
            @JsonProperty("systemBoxCode")
            private String systemBoxCode;
            @JsonProperty("boxLength")
            private String boxLength;
            @JsonProperty("boxWidth")
            private String boxWidth;
            @JsonProperty("boxHeight")
            private String boxHeight;
            @JsonProperty("boxWeight")
            private String boxWeight;
            @JsonProperty("boxActualLength")
            private String boxActualLength;
            @JsonProperty("boxActualWidth")
            private String boxActualWidth;
            @JsonProperty("boxActualHeight")
            private String boxActualHeight;
            @JsonProperty("boxActualWeight")
            private String boxActualWeight;
            @JsonProperty("status")
            private String status;
            @JsonProperty("boxRemark")
            private Object boxRemark;
            @JsonProperty("sellerId")
            private Object sellerId;
            @JsonProperty("pltWarehouseCode")
            private Object pltWarehouseCode;
            @JsonProperty("finishPutawayTime")
            private String finishPutawayTime;
            @JsonProperty("inboundSkuList")
            private List<InboundSkuListDTO> inboundSkuList;

            @NoArgsConstructor
            @Data
            public static class InboundSkuListDTO {
                @JsonProperty("skuCode")
                private String skuCode;
                @JsonProperty("quantity")
                private Integer quantity;
                @JsonProperty("checkQty")
                private Integer checkQty;
                @JsonProperty("putawayQty")
                private Integer putawayQty;
            }
        }
    }
}
