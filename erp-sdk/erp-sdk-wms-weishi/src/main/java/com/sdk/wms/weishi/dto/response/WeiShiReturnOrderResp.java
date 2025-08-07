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
public class WeiShiReturnOrderResp {

    @JSONField(name = "pageNo")
    private Integer pageNo;
    @JSONField(name = "pageSize")
    private Integer pageSize;
    @JSONField(name = "totalPage")
    private Integer totalPage;
    @JSONField(name = "totalSize")
    private Integer totalSize;
    @JSONField(name = "rows")
    private List<RowsDTO> rows;
    @JSONField(name = "heads")
    private Object heads;

    @NoArgsConstructor
    @Data
    public static class RowsDTO {

        private String authId;

        @JSONField(name = "returnId")
        private String returnId;
        @JSONField(name = "returnType")
        private Integer returnType;
        @JSONField(name = "packageNo")
        private String packageNo;
        @JSONField(name = "erpNo")
        private String erpNo;
        @JSONField(name = "platformOrderNo")
        private String platformOrderNo;
        @JSONField(name = "trackingNo")
        private String trackingNo;
        @JSONField(name = "warehouseCode")
        private String warehouseCode;
        @JSONField(name = "status")
        private Integer status;
        @JSONField(name = "createTime")
        private String createTime;
        @JSONField(name = "finishTime")
        private String finishTime;
        @JSONField(name = "cancelTime")
        private String cancelTime;
        @JSONField(name = "skuList")
        private List<SkuListDTO> skuList;

        @NoArgsConstructor
        @Data
        public static class SkuListDTO {
            @JSONField(name = "sku")
            private String sku;
            @JSONField(name = "returnCount")
            private Integer returnCount;
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
