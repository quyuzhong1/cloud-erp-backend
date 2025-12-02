package com.sdk.wms.weishi.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
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
public class WeiShiReturnInstockResp {

    @JSONField(name = "total")
    private Integer total;
    @JSONField(name = "rows")
    private List<RowsDTO> rows;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class RowsDTO {
        @JSONField(name = "rtOrderCode")
        private String rtOrderCode;
        @JSONField(name = "sourceCode")
        private String sourceCode;
        @JSONField(name = "receivingCode")
        private String receivingCode;
        @JSONField(name = "platformCode")
        private String platformCode;
        @JSONField(name = "trackingNo")
        private String trackingNo;
        @JSONField(name = "whsCode")
        private String whsCode;
        @JSONField(name = "createTime")
        private String createTime;
        @JSONField(name = "receiveTime")
        private String receiveTime;
        @JSONField(name = "completionTime")
        private String completionTime;
        @JSONField(name = "createType")
        private Integer createType;
        @JSONField(name = "returnType")
        private Integer returnType;
        @JSONField(name = "status")
        private Integer status;
        @JSONField(name = "skuList")
        private List<SkuListDTO> skuList;
        @JSONField(name = "incomeList")
        private List<IncomeListDTO> incomeList;

        @NoArgsConstructor
        @Data
        public static class SkuListDTO {
            @JSONField(name = "rtOrderCode")
            private String rtOrderCode;
            @JSONField(name = "productSku")
            private String productSku;
            @JSONField(name = "referenceNo")
            private String referenceNo;
            @JSONField(name = "productTitle")
            private String productTitle;
            @JSONField(name = "productTitleEn")
            private String productTitleEn;
            @JSONField(name = "orderQty")
            private Integer orderQty;
            @JSONField(name = "countedQty")
            private Integer countedQty;
            @JSONField(name = "handleQty")
            private Integer handleQty;
            @JSONField(name = "destroyedQty")
            private Integer destroyedQty;
            @JSONField(name = "processMode")
            private Integer processMode;
            @JSONField(name = "photoList")
            private Object photoList;
            @JSONField(name = "height")
            private String height;
            @JSONField(name = "width")
            private String width;
            @JSONField(name = "length")
            private String length;
            @JSONField(name = "weight")
            private String weight;
            @JSONField(name = "blGood")
            private Integer blGood;
        }

        @NoArgsConstructor
        @Data
        public static class IncomeListDTO {
            @JSONField(name = "fkName")
            private String fkName;
            @JSONField(name = "value")
            private String value;
            @JSONField(name = "currencyCode")
            private String currencyCode;
            @JSONField(name = "currencyRate")
            private String currencyRate;
            @JSONField(name = "currencyRateValue")
            private String currencyRateValue;
            @JSONField(name = "occurDate")
            private String occurDate;
        }
    }
}
