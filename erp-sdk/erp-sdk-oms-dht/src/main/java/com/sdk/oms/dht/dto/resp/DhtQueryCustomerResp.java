package com.sdk.oms.dht.dto.resp;

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
public class DhtQueryCustomerResp  {

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
        @JsonProperty("account_status")
        private String accountStatus;
        @JSONField(name = "erp_number__c")
        private String erpCustomerCode;

        @JsonProperty("_id")
        private String id;
    }
}
