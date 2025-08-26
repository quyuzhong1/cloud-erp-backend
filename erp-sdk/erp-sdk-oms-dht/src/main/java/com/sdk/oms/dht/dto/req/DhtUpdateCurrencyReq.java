
package com.sdk.oms.dht.dto.req;

import com.alibaba.fastjson.annotation.JSONField;
import com.sdk.oms.dht.dto.BaseReq;
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
public class DhtUpdateCurrencyReq extends BaseReq {

    @JSONField(name = "data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DataDTO {
        @JSONField(name = "exchangeRateList")
        private List<ExchangeRateListDTO> exchangeRateList;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ExchangeRateListDTO {
            @JSONField(name = "exchangeRate")
            private String exchangeRate;
            @JSONField(name = "currencyCode")
            private String currencyCode;
        }
    }
}
