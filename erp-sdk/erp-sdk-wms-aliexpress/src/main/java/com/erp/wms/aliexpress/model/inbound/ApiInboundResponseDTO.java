package com.erp.wms.aliexpress.model.inbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiInboundResponseDTO {
    // Getters & Setters
    // 公共字段（成功/失败都存在）
    @JsonProperty("request_id")
    private String requestId;

    // 成功响应字段
    private Result result;

    // 失败响应字段
    @JsonProperty("error_response")
    private ErrorResponse errorResponse;

    // 判断是否成功的便捷方法
    public boolean isSuccess() {
        return result != null && result.success;
    }

    // 嵌套类：成功响应结构
    @Setter
    @Getter
    public static class Result {
        private Data data;
        private boolean success;

        // 嵌套类：数据字段
        @Setter
        @Getter
        public static class Data {
            @JsonProperty("entry_order_id")
            private String entryOrderId;

        }

        // Getters & Setters
        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    // 嵌套类：错误响应结构
    public static class ErrorResponse {
        private String code;
        private String type;
        @JsonProperty("sub_code")
        private String subCode;
        @JsonProperty("sub_msg")
        private String subMsg;
        private String msg;

        // Getters & Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSubCode() { return subCode; }
        public void setSubCode(String subCode) { this.subCode = subCode; }
        public String getSubMsg() { return subMsg; }
        public void setSubMsg(String subMsg) { this.subMsg = subMsg; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
    }

}
