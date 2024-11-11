package com.erp.server.dmp.handler;

import com.erp.model.dmp.track123.WebhookRequest;

/**
 * @author zdy
 * @ClassName WebhookHandler
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
public interface WebhookHandler {

    void process(WebhookRequest request);
}
