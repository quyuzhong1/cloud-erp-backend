package com.sdk.oms.dht.dto.resp;

import com.alibaba.fastjson.annotation.JSONField;
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
public class DhtQueryCustomerAccountResp {

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

        /**
         * 账户余额
         */
        @JsonProperty("account_balance")
        private BigDecimal accountBalance;

        /**
         * 占用金额
         */
        @JsonProperty("occupied_amount")
        private BigDecimal occupiedAmount;

        @JsonProperty("name")
        private String name;

        @JsonProperty("customer_id")
        private String customerId;

        @JsonProperty("fund_account_id")
        private String fundAccountId;

        @JsonProperty("life_status")
        private String lifeStatus;

        @JsonProperty("_id")
        private String id;
    }
}
