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
public class DhtOperationCustomerReq extends BaseReq {

    @Alias("hasSpecifyTime")
    private Boolean hasSpecifyTime;
    @Alias("includeDetailIds")
    private Boolean includeDetailIds;
    @Alias("data")
    private DataDTO data;
    @Alias("hasSpecifyCreatedBy")
    private Boolean hasSpecifyCreatedBy;

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
            //id，更新时必填，新增时不填
            @Alias("_id")
            private String id;
            @Alias("object_describe_api_name")
            private String objectDescribeApiName;
            @Alias("name")
            private String name;
            @Alias("tel")
            private String tel;
            /**
             * 客户编号
             */
            @Alias("erp_number__c")
            private String erpCustomerCode;
            @Alias("settlement_currency__c")
            private String currency;
            @Alias("country")
            private String country;
            @Alias("sales_organization__c")
            private String salesOrganization;
            @Alias("dataObjectApiName")
            private String dataObjectApiName;
            @Alias("record_type")
            private String recordType;
            @Alias("customer_status__c")
            private String customerStatus;
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
