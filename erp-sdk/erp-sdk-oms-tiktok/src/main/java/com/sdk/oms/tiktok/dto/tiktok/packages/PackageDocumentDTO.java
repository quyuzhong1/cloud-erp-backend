package com.sdk.oms.tiktok.dto.tiktok.packages;

import lombok.Data;

import java.util.List;

@Data
public class PackageDocumentDTO {
    private int code; // 响应码
    private ResponseData data; // 数据部分
    private String message; // 响应消息
    private String request_id; // 请求ID

    @Data
    public static class ResponseData{
        private String docUrl;
        private String trackingNumber;
    }

}
