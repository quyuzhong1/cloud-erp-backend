package com.sdk.oms.dht.dto.req;

import cn.hutool.core.annotation.Alias;
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

    @Alias("data")
    private DhtOperationCustomerAddressReq.DataDTO data;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DataDTO {

        @Alias("object_data")
        private ObjectDataDTO objectData;
        @Alias("optionInfo")
        private OptionInfoDTO optionInfo;
        @Alias("details")
        private DetailsDTO details;
        @Alias("igonreMediaIdConvert")
        private Boolean igonreMediaIdConvert;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ObjectDataDTO {

            @Alias("_id")
            private String id;
            @Alias("account_id")
            private String accountId;
            @Alias("object_describe_api_name")
            private String objectDescribeApiName;
            @Alias("name")
            private String name;
            @Alias("dataObjectApiName")
            private String dataObjectApiName;
            @Alias("record_type")
            private String recordType;
            @Alias("add_type")
            private String addType;

            @Alias("address")
            private String address;

            @Alias("remark")
            private String remark;

            @Alias("erp_customer_address_code__c")
            private String erpCustomerAddressCode;

            /**
             * 是否默认地址
             */
            @Alias("is_ship_to_add")
            private Boolean isDefaultAddress;

            /**
             * 联系方式
             */
            @Alias("contact_way")
            private String phone;
        }

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class OptionInfoDTO {
            @Alias("skipFuncValidate")
            private Boolean skipFuncValidate;
            @Alias("isDuplicateSearch")
            private Boolean isDuplicateSearch;
            @Alias("useValidationRule")
            private Boolean useValidationRule;
        }

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class DetailsDTO {
            @Alias("detail_api_name")
            private DetailApiNameDTO detailApiName;

            @NoArgsConstructor
            @Data
            public static class DetailApiNameDTO {
            }
        }
    }

}
