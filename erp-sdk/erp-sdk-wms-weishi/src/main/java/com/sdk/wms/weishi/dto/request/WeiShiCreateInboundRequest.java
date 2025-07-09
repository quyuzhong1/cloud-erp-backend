package com.sdk.wms.weishi.dto.request;

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
public class WeiShiCreateInboundRequest {

    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "trackingNo")
    private String trackingNo;
    @JSONField(name = "expectedTime")
    private String expectedTime;
    @JSONField(name = "warehouse")
    private String warehouse;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "receiptType")
    private Integer receiptType;
    @JSONField(name = "skuList")
    private List<SkuListDTO> skuList;
    @JSONField(name = "boxList")
    private List<BoxListDTO> boxList;

    @NoArgsConstructor
    @Data
    public static class SkuListDTO {
        @JSONField(name = "sku")
        private String sku;
        @JSONField(name = "count")
        private Integer count;
    }

    @NoArgsConstructor
    @Data
    public static class BoxListDTO {
        @JSONField(name = "length")
        private Integer length;
        @JSONField(name = "width")
        private Integer width;
        @JSONField(name = "height")
        private Integer height;
        @JSONField(name = "weight")
        private Integer weight;
        @JSONField(name = "skuVos")
        private List<SkuVosDTO> skuVos;

        @NoArgsConstructor
        @Data
        public static class SkuVosDTO {
            @JSONField(name = "sku")
            private String sku;
            @JSONField(name = "count")
            private Integer count;
        }
    }
}
