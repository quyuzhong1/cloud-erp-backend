package com.sdk.wms.damai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DaMaiCreateInboundRequest {

    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("custReferenceNo")
    private String custReferenceNo;
    @JsonProperty("shippingName")
    private String shippingName;
    @JsonProperty("shippingTel")
    private String shippingTel;
    @JsonProperty("anTrayQty")
    private String anTrayQty;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("arrivalTime")
    private String arrivalTime;
    @JsonProperty("transportType")
    private String transportType;
    @JsonProperty("logisticsTrackingNo")
    private String logisticsTrackingNo;
    @JsonProperty("asnAnSkuList")
    private List<AsnAnSkuListDTO> asnAnSkuList;
    @JsonProperty("snList")
    private List<SnListDTO> snList;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class AsnAnSkuListDTO {
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        @JsonProperty("custLotNo")
        private String custLotNo;
        @JsonProperty("custPackageNo")
        private String custPackageNo;
        @JsonProperty("totalSkuQty")
        private Integer totalSkuQty;
        @JsonProperty("packQty")
        private Integer packQty;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class SnListDTO {
        @JsonProperty("custSkuCode")
        private String custSkuCode;
        @JsonProperty("sn")
        private String sn;
    }
}
