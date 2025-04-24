package com.erp.server.dmp.service;

import com.erp.model.dmp.track123.WebhookRequest;

/**
 * @author zdy
 * @ClassName WebhookService
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
public interface WebhookService {

    void handleRequest(WebhookRequest request);
}
