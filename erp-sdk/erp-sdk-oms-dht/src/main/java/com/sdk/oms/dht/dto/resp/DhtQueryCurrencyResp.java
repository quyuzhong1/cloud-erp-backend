package com.sdk.oms.dht.dto.resp;

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
public class DhtQueryCurrencyResp {


    @JSONField(name = "currencyList")
    private List<CurrencyListDTO> currencyList;

    @NoArgsConstructor
    @Data
    public static class CurrencyListDTO {
        @JSONField(name = "id")
        private String id;
        @JSONField(name = "tenantId")
        private String tenantId;
        @JSONField(name = "currencyCode")
        private String currencyCode;
        @JSONField(name = "currencyLabel")
        private String currencyLabel;
        @JSONField(name = "isFunctional")
        private Boolean isFunctional;
        @JSONField(name = "status")
        private Integer status;
        @JSONField(name = "objectDescribeApiName")
        private String objectDescribeApiName;
        @JSONField(name = "createTime")
        private Long createTime;
        @JSONField(name = "createBy")
        private String createBy;
        @JSONField(name = "lastModifiedTime")
        private Long lastModifiedTime;
        @JSONField(name = "lastModifiedBy")
        private String lastModifiedBy;
        @JSONField(name = "exchangeRate")
        private String exchangeRate;
        @JSONField(name = "exchangeRateVersion")
        private String exchangeRateVersion;
        @JSONField(name = "currencyPrefix")
        private String currencyPrefix;
        @JSONField(name = "currencySuffix")
        private String currencySuffix;
        @JSONField(name = "currencySymbol")
        private String currencySymbol;
        @JSONField(name = "currencyUnit")
        private String currencyUnit;
        @JSONField(name = "currencyType")
        private String currencyType;
        @JSONField(name = "customCurrency")
        private Boolean customCurrency;
        @JSONField(name = "realCurrencyPrefix")
        private String realCurrencyPrefix;
        @JSONField(name = "realCurrencySuffix")
        private String realCurrencySuffix;
        @JSONField(name = "i18NCurrencyLabel")
        private String i18NCurrencyLabel;
    }
}
