package com.sdk.wms.jitu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
public class OverseasInboundCreateResponse extends BaseResponse {

    private List<Response> responseitems;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String success;
        private String reason;
        private String errorMsg;
        private String message;
        //客户订单号
        private String entryOrderCode;
        //入库单号
        private String entryOrderId;
    }
}
