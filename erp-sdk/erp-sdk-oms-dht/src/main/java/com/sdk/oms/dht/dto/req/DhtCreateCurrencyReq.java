
package com.sdk.oms.dht.dto.req;

import com.alibaba.fastjson.annotation.JSONField;
import com.sdk.oms.dht.dto.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DhtCreateCurrencyReq extends BaseReq {

    @JSONField(name = "data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DataDTO {
        @JSONField(name = "exchangeRate")
        private String exchangeRate;
        @JSONField(name = "currencyCode")
        private String currencyCode;
    }
}
