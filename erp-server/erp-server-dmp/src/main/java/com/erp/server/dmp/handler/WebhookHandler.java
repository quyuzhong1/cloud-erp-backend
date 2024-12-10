package com.erp.server.dmp.handler;

import com.erp.model.dmp.track123.WebhookRequest;

import java.util.Map;

/**
 * @author zdy
 * @ClassName WebhookHandler
 * @description: Webhook处理
 * @date 2024年11月11日
 * @version: 1.0
 */
public interface WebhookHandler {
    /**
     * 校验安全性
     * @param data
     * @param headers
     * @param serviceFlag
     */
    void verify(String data, Map<String, String> headers, String serviceFlag);
    /**
     *业务处理
     * @param data
     * @param headers
     * @param serviceFlag
     */
    String process(String data, Map<String, String> headers, String serviceFlag);
}
