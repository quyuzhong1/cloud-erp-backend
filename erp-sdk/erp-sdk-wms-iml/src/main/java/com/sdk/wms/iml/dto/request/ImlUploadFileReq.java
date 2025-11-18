package com.sdk.wms.iml.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImlUploadFileReq {

    @JSONField(name = "platformCustomerCode")
    private String platformCustomerCode;
    @JSONField(name = "fileNumber")
    private String fileNumber;
    @JSONField(name = "fileName")
    private String fileName;
    @JSONField(name = "filePath")
    private String filePath;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "type")
    private String type;
    @JSONField(name = "orderList")
    private List<OrderListDTO> orderList;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderListDTO {
        @JSONField(name = "orderNo")
        private String orderNo;
        @JSONField(name = "platformOrderSkuInfoList")
        private List<PlatformOrderSkuInfoListDTO> platformOrderSkuInfoList;

        @NoArgsConstructor
        @Data
        public static class PlatformOrderSkuInfoListDTO {
            @JSONField(name = "platformOrderNo")
            private String platformOrderNo;
            @JSONField(name = "skuBarcode")
            private String skuBarcode;
            @JSONField(name = "skuOnlyCode")
            private String skuOnlyCode;
            @JSONField(name = "sku")
            private String sku;
            @JSONField(name = "skuNameCn")
            private String skuNameCn;
            @JSONField(name = "skuNameEn")
            private String skuNameEn;
            @JSONField(name = "quantity")
            private Integer quantity;
        }
    }
}
