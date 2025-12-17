package com.sdk.oms.dht.dto.resp;

import cn.hutool.core.annotation.Alias;
import com.alibaba.fastjson.annotation.JSONField;
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
public class DhtQueryCustomerAddressResp {

    @JsonProperty("dataList")
    private List<DataListDTO> dataList;
    @JsonProperty("offset")
    private Integer offset;
    @JsonProperty("limit")
    private Integer limit;
    @JsonProperty("total")
    private Integer total;

    @NoArgsConstructor
    @Data
    public static class DataListDTO {
        @JsonProperty("account_no")
        private String accountNo;
        @JsonProperty("name")
        private String name;
        @JsonProperty("record_type")
        private String recordType;
        @JsonProperty("life_status")
        private String lifeStatus;
        @JSONField(name = "erp_customer_address_code__c")
        private String erpCustomerAddressCode;

        @JsonProperty("_id")
        private String id;
    }
}
