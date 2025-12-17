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
public class WeiShiStockAgeResp {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("list")
    private List<ListDTO> list;

    @NoArgsConstructor
    @Data
    public static class ListDTO {
        @JsonProperty("number")
        private Object number;
        @JsonProperty("id")
        private String id;
        @JsonProperty("skuProductId")
        private String skuProductId;
        @JsonProperty("productSku")
        private String productSku;
        @JsonProperty("cusProductCodeSet")
        private String cusProductCodeSet;
        @JsonProperty("productTitle")
        private String productTitle;
        @JsonProperty("productTitleEn")
        private String productTitleEn;
        @JsonProperty("referenceNo")
        private String referenceNo;
        @JsonProperty("cusProductCode")
        private String cusProductCode;
        @JsonProperty("customerId")
        private Integer customerId;
        @JsonProperty("customerCode")
        private String customerCode;
        @JsonProperty("customerShortName")
        private String customerShortName;
        @JsonProperty("areaCode")
        private String areaCode;
        @JsonProperty("warehouseId")
        private String warehouseId;
        @JsonProperty("warehouseCode")
        private String warehouseCode;
        @JsonProperty("shelvesDate")
        private String shelvesDate;
        @JsonProperty("storageDay")
        private Integer storageDay;
        @JsonProperty("shelvesQty")
        private Integer shelvesQty;
        @JsonProperty("csSellerId")
        private String csSellerId;
        @JsonProperty("csSellerName")
        private String csSellerName;
        @JsonProperty("idsStr")
        private String idsStr;
        @JsonProperty("idList")
        private String idList;
        @JsonProperty("groupByStr")
        private String groupByStr;
        @JsonProperty("storageType")
        private String storageType;
    }
}
