package com.sdk.wms.damai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiCalculateFeeResp {

    @JsonProperty("productCode")
    private String productCode;
    @JsonProperty("productName")
    private String productName;
    @JsonProperty("currencyCode")
    private String currencyCode;
    @JsonProperty("billAmount")
    private BigDecimal billAmount;
    @JsonProperty("baseFreightAmount")
    private BigDecimal baseFreightAmount;
    @JsonProperty("otherAmount")
    private BigDecimal otherAmount;
    @JsonProperty("totalBillableWeight")
    private BigDecimal totalBillableWeight;
    @JsonProperty("errMsg")
    private String errMsg;
    @JsonProperty("itemList")
    private List<ItemListDTO> itemList;

    @NoArgsConstructor
    @Data
    public static class ItemListDTO {
        @JsonProperty("currencyCode")
        private String currencyCode;
        @JsonProperty("feeItemName")
        private String feeItemName;
        @JsonProperty("feeItemCode")
        private String feeItemCode;
        @JsonProperty("feeAmount")
        private BigDecimal feeAmount;
    }
}
