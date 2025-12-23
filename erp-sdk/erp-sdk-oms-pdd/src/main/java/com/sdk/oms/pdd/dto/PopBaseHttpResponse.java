package com.sdk.oms.pdd.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class PopBaseHttpResponse {
    @JSONField(name ="error_response")
    private ErrorResponse errorResponse;
    @JSONField(name ="request_id")
    private String requestId;

    public PopBaseHttpResponse() {
    }

    @Data
    public static class ErrorResponse {
        @JSONField(name ="request_id")
        private String requestId;
        @JSONField(name ="error_code")
        private Integer errorCode;
        @JSONField(name ="error_msg")
        private String errorMsg;
        @JSONField(name ="sub_code")
        private String subCode;
        @JSONField(name ="sub_msg")
        private String subMsg;

        public ErrorResponse() {
        }

    }
}
