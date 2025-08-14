package com.sdk.wms.weishi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiStockAgeRequest extends WeiShiBaseRequest{

    @JsonProperty("skuList")
    private List<SkuListDTO> skuList;
    @JsonProperty("startShelvesDate")
    private String startShelvesDate;
    @JsonProperty("endShelvesDate")
    private String endShelvesDate;

    @NoArgsConstructor
    @Data
    public static class SkuListDTO {
        @JsonProperty("skuCode")
        private String skuCode;
        @JsonProperty("defectiveSku")
        private String defectiveSku;
    }
}
