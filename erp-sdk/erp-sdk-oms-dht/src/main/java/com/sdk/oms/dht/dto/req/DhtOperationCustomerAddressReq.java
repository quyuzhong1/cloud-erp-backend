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
public class DhtOperationCustomerAddressReq extends BaseReq {


    @JSONField(name = "object_data")
    private ObjectDataDTO objectData;
    @JSONField(name = "optionInfo")
    private OptionInfoDTO optionInfo;
    @JSONField(name = "details")
    private DetailsDTO details;
    @JSONField(name = "igonreMediaIdConvert")
    private Boolean igonreMediaIdConvert;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ObjectDataDTO {
        @JSONField(name = "account_id")
        private String accountId;
        @JSONField(name = "object_describe_api_name")
        private String objectDescribeApiName;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "dataObjectApiName")
        private String dataObjectApiName;
        @JSONField(name = "record_type")
        private String recordType;
        @JSONField(name = "add_type")
        private String addType;

        @JSONField(name = "erp_customer_address_code__c")
        private String erpCustomerAddressCode;

        /**
         * 是否默认地址
         */
        @JSONField(name = "is_ship_to_add")
        private Boolean isDefaultAddress;

        /**
         * 联系方式
         */
        @JSONField(name = "contact_way")
        private String phone;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionInfoDTO {
        @JSONField(name = "skipFuncValidate")
        private Boolean skipFuncValidate;
        @JSONField(name = "isDuplicateSearch")
        private Boolean isDuplicateSearch;
        @JSONField(name = "useValidationRule")
        private Boolean useValidationRule;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DetailsDTO {
        @JSONField(name = "detail_api_name")
        private DetailApiNameDTO detailApiName;

        @NoArgsConstructor
        @Data
        public static class DetailApiNameDTO {
        }
    }
}
