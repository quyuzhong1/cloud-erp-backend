package com.common.business.handler;

/**
 * 业务处理器
 * @author Cloud
 * @param <T>
 */
public interface BusinessHandler<T> {
    /**
     * 处理业务
     * @param data
     */
    void handle(T data);
}