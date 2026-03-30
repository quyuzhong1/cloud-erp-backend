package com.sdk.oms.tiktok.dto.tiktok.order;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GlobalWarehouseDTO {

    @JSONField(name = "code")
    private Integer code;

    @JSONField(name = "data")
    private DataDTO data;

    @JSONField(name = "message")
    private String message;

    @JSONField(name = "request_id")
    private String requestId;

    @NoArgsConstructor
    @Data
    public static class DataDTO {

        @JSONField(name = "global_warehouses")
        private List<GlobalWarehousesDTO> globalWarehouses;

        @NoArgsConstructor
        @Data
        public static class GlobalWarehousesDTO {

            @JSONField(name = "id")
            private String id;

            @JSONField(name = "name")
            private String name;

            @JSONField(name = "ownership")
            private String ownership;
        }
    }
}
