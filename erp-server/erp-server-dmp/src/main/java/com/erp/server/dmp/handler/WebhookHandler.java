package com.erp.server.dmp.handler;

import com.erp.model.dmp.track123.WebhookRequest;

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
     * @param request
     */
    void verify(String request);
    /**
     *业务处理
     * @param request
     */
    void process(String request);
}
