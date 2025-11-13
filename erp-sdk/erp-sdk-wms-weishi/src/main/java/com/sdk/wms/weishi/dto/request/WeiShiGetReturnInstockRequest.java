package com.sdk.wms.weishi.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
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
public class WeiShiGetReturnInstockRequest {


    @JSONField(name = "orderNoList")
    private List<String> orderNoList;
    @JSONField(name = "pageNum")
    private Integer pageNum;
    @JSONField(name = "pageSize")
    private Integer pageSize;
    @JSONField(name = "queryTime")
    private QueryTimeDTO queryTime;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class QueryTimeDTO {
        @JSONField(name = "startTime")
        private String startTime;
        @JSONField(name = "endTime")
        private String endTime;
        @JSONField(name = "eventType")
        private String eventType;
    }
}
