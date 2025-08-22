
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
public class DhtEnableCustomerReq extends BaseReq {

    @Alias("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class DataDTO {

        @Alias("dataObjectApiName")
        private String dataObjectApiName;

        @Alias("idList")
        private List<String> idList;
    }
}
