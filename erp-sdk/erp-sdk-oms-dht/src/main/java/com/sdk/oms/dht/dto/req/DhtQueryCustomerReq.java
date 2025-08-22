package com.sdk.oms.dht.dto.req;

import cn.hutool.core.annotation.Alias;
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
public class DhtQueryCustomerReq extends BaseReq {

    @Alias("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DataDTO {
        @Alias("find_explicit_total_num")
        private Boolean findExplicitTotalNum;
        @Alias("search_query_info")
        private SearchQueryInfoDTO searchQueryInfo;
        @Alias("dataObjectApiName")
        private String dataObjectApiName;

        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class SearchQueryInfoDTO {
            @Alias("offset")
            private Integer offset;
            @Alias("limit")
            private Integer limit;
            @Alias("orders")
            private List<OrdersDTO> orders;
            @Alias("fieldProjection")
            private List<String> fieldProjection;
            @Alias("filters")
            private List<FiltersDTO> filters;

            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            @Builder
            public static class OrdersDTO {
                @Alias("fieldName")
                private String fieldName;
                @Alias("isAsc")
                private Boolean isAsc;
            }

            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            @Builder
            public static class FiltersDTO {
                @Alias("operator")
                private String operator;
                @Alias("field_name")
                private String fieldName;
                @Alias("field_values")
                private List<String> fieldValues;
            }
        }
    }
}
