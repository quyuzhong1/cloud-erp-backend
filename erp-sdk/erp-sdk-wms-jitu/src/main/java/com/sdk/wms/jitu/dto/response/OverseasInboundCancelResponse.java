package com.sdk.wms.jitu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zdy
 * @ClassName JituOverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasInboundCancelResponse extends BaseResponse {

    private List<Response> responseitems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String success;
        private String reason;
        private String message;
        private String errorMsg;
    }
}
