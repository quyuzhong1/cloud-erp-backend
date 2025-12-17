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
public class WeiShiStockResp {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("list")
        private List<ListDTO> list;

        @NoArgsConstructor
        @Data
        public static class ListDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("skuId")
            private String skuId;
            @JsonProperty("skuCode")
            private String skuCode;
            @JsonProperty("skuSearch")
            private Object skuSearch;
            @JsonProperty("referenceNo")
            private String referenceNo;
            @JsonProperty("warehouseId")
            private Integer warehouseId;
            @JsonProperty("warehouseCode")
            private String warehouseCode;
            @JsonProperty("skuCnName")
            private String skuCnName;
            @JsonProperty("skuEnName")
            private String skuEnName;
            @JsonProperty("totalQty")
            private Integer totalQty;
            @JsonProperty("availableQty")
            private Integer availableQty;
            @JsonProperty("frozenQty")
            private Integer frozenQty;
            @JsonProperty("waitOutBoundQty")
            private Integer waitOutBoundQty;
            @JsonProperty("lackQty")
            private Integer lackQty;
            @JsonProperty("customerId")
            private Integer customerId;
            @JsonProperty("customerCode")
            private String customerCode;
            @JsonProperty("customerShortName")
            private String customerShortName;
            @JsonProperty("blQualifiedProduct")
            private Boolean blQualifiedProduct;
            @JsonProperty("storageType")
            private String storageType;
            @JsonProperty("revision")
            private Integer revision;
            @JsonProperty("createBy")
            private String createBy;
            @JsonProperty("createTime")
            private String createTime;
            @JsonProperty("updateBy")
            private Object updateBy;
            @JsonProperty("updateTime")
            private Object updateTime;
            @JsonProperty("lastStockUpdateDate")
            private String lastStockUpdateDate;
        }
    }
}
