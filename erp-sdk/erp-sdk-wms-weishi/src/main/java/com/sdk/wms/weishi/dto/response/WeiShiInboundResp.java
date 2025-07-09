package com.sdk.wms.weishi.dto.response;

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
public class WeiShiInboundResp {

    @JSONField(name = "warehouseCode")
    private String warehouseCode;
    @JSONField(name = "inboundNo")
    private String inboundNo;
    @JSONField(name = "status")
    private Integer status;
    @JSONField(name = "signTime")
    private String signTime;
    @JSONField(name = "receiveLastTime")
    private String receiveLastTime;
    @JSONField(name = "putawayLastTime")
    private String putawayLastTime;
    @JSONField(name = "finishTime")
    private String finishTime;
    @JSONField(name = "receiveStatus")
    private Integer receiveStatus;
    @JSONField(name = "putawayStatus")
    private Integer putawayStatus;
    @JSONField(name = "checkOutLogistics")
    private String checkOutLogistics;
    @JSONField(name = "checkOutTrackingNo")
    private String checkOutTrackingNo;
    @JSONField(name = "forceFinish")
    private Integer forceFinish;
    @JSONField(name = "receiptType")
    private Integer receiptType;
    @JSONField(name = "trackingNoList")
    private List<String> trackingNoList;
    @JSONField(name = "skuList")
    private List<SkuListDTO> skuList;
    @JSONField(name = "boxList")
    private List<BoxListDTO> boxList;

    @NoArgsConstructor
    @Data
    public static class SkuListDTO {
        private String putawayLastTime;
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "count")
        private Integer count;
        @JSONField(name = "receiveCount")
        private Integer receiveCount;
        @JSONField(name = "putawayCount")
        private Integer putawayCount;
        @JSONField(name = "goodCount")
        private Integer goodCount;
        @JSONField(name = "badCount")
        private Integer badCount;
        @JSONField(name = "actualWeight")
        private Integer actualWeight;
        @JSONField(name = "actualVolume")
        private Integer actualVolume;
    }

    @NoArgsConstructor
    @Data
    public static class BoxListDTO {

        @JSONField(name = "boxNumber")
        private Integer boxNumber;
        @JSONField(name = "boxNo")
        private String boxNo;
        @JSONField(name = "skuCount")
        private Integer skuCount;
        @JSONField(name = "length")
        private Integer length;
        @JSONField(name = "width")
        private Integer width;
        @JSONField(name = "height")
        private Integer height;
        @JSONField(name = "weight")
        private Integer weight;
        @JSONField(name = "skuList")
        private List<SkuListDTO> skuList;

        @NoArgsConstructor
        @Data
        public static class SkuListDTO {
            @JSONField(name = "sku")
            private String sku;
            @JSONField(name = "count")
            private Integer count;
            @JSONField(name = "receiveCount")
            private Integer receiveCount;
            @JSONField(name = "putawayCount")
            private Integer putawayCount;
            @JSONField(name = "goodCount")
            private Integer goodCount;
            @JSONField(name = "badCount")
            private Integer badCount;
            @JSONField(name = "actualWeight")
            private Integer actualWeight;
            @JSONField(name = "actualVolume")
            private Integer actualVolume;
        }
    }
}
