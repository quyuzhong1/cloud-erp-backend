package com.erp.server.dmp.factory;

import com.erp.server.dmp.handler.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @ClassName WebhookHandlerFactory
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Component
public class WebhookHandlerFactory {
    private final Map<String, WebhookHandler> handlerMap = new HashMap<>();

    public WebhookHandlerFactory() {
        handlerMap.put("track123", new Track123WebhookHandler());
        handlerMap.put("outbound", new OrderOutboundHandler());
        handlerMap.put("inbound", new InboundHandler());
        handlerMap.put("returnInstock", new ReturnInstockHandler());
        handlerMap.put("jituOverseasInbound", new JituOverseasInboundHandler());
        handlerMap.put("jituOutbound", new JituOutboundHandler());
        handlerMap.put("qimenCallback", new QimenCallbackHandler());
        handlerMap.put("shopee", new ShopeeWebhookHandler());
    }

    public WebhookHandler getHandler(String service) {
        WebhookHandler handler = handlerMap.get(service);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown platform");
        }
        return handler;
    }
}
