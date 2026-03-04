package com.sdk.wms.zhongbao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author zdy
 * @ClassName BaseResponse
 * @description: 基础响应
 * @date 2026年03月02日
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseResponse<T> {
    //状态码
    private String code;
    //成功与否
    private Boolean success;
    private T data;
    //提示消息
    private String message;
    //错误消息列表
    private List<String> errors;
    //请求时间
    private String timestamp;
    //请求耗时：秒
    private Double duration;
    //请求ID
    private String requestId;

    public static BaseResponse error(String msg) {
        return BaseResponse.builder()
                .code("-1")
                .message(msg)
                .success(false)
                .build();
    }
}
