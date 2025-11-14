package com.sdk.oms.dht.dto.req;

import cn.hutool.core.annotation.Alias;
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
public class DhtSimpleQueryReq extends BaseReq {


    @Alias("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static class DataDTO {
        @Alias("search_query_info")
        private SearchQueryInfoDTO searchQueryInfo;
        @Alias("dataObjectApiName")
        private String dataObjectApiName;
        @Alias("igonreMediaIdConvert")
        private Boolean igonreMediaIdConvert;
        @Alias("field_projection")
        private List<String> fieldProjection;

        @NoArgsConstructor
        @Data
        @Builder
        @AllArgsConstructor
        public static class SearchQueryInfoDTO {
            @Alias("offset")
            private String offset;
            @Alias("limit")
            private String limit;
            @Alias("orders")
            private List<OrdersDTO> orders;
            @Alias("filters")
            private List<FiltersDTO> filters;

            @NoArgsConstructor
            @Data
            @Builder
            @AllArgsConstructor
            public static class FiltersDTO {
                @Alias("operator")
                private String operator;
                @Alias("field_name")
                private String fieldName;
                @Alias("field_values")
                private List<String> fieldValues;
            }

            @NoArgsConstructor
            @Data
            @Builder
            @AllArgsConstructor
            public static class OrdersDTO {
                @Alias("fieldName")
                private String fieldName;
                @Alias("isAsc")
                private String isAsc;
            }
        }
    }
}
