package com.erp.sdk.oms.yunting.cem.client;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 云听CEM API响应封装类
 *
 * @param <T> 响应数据类型
 * @author ERP System
 */
@Getter
public class ApiResponse<T> {
    
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final T data;

    /**
     * 构造API响应
     *
     * @param statusCode HTTP状态码
     * @param headers    HTTP响应头
     */
    public ApiResponse(int statusCode, Map<String, List<String>> headers) {
        this(statusCode, headers, null);
    }

    /**
     * 构造API响应
     *
     * @param statusCode HTTP状态码
     * @param headers    HTTP响应头
     * @param data       响应数据
     */
    public ApiResponse(int statusCode, Map<String, List<String>> headers, T data) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }
}

