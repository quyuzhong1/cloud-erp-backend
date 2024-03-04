package com.sdk.third.lingxing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;

/**
 * 领星接口响应结果
 * @param <T>
 */
@Data
@NoArgsConstructor
@ToString
public class Result<T> {

    /**
     * 自定义业务码(必有)
     */
    private String code;

    /**
     * 自定义返回 数据结果集(必有)
     */
    private T data;

    /**
     * 自定义业务提示说明
     */
    private String msg;

    /**
     * 消息提示
     */
    private String message;

    /**
     * 错误信息
     */
    private Collection<?> error_details;

    /**
     * 请求链路id
     */
    private String request_id;

    /**
     * 响应时间
     */
    private String response_time;

    /**
     * 总数量
     */
    private Integer total;
}
