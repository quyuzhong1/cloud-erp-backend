package com.sdk.wms.jitu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zdy
 * @ClassName WarehouseResponse
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse extends BaseResponse {
    private List<Response> responseitems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String success;
        private String reason;
        private String errorMsg;
        private List<Warehouse> baseList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Warehouse {
        //仓库代码
        private String warehouseCode;
        private String warehouseName;
    }
}
