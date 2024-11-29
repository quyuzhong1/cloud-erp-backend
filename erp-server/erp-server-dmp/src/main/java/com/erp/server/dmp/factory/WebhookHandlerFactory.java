package com.erp.server.dmp.factory;

import com.erp.server.dmp.handler.Track123WebhookHandler;
import com.erp.server.dmp.handler.WebhookHandler;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @ClassName WebhookHandlerFactory
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Component
public class WebhookHandlerFactory {
    public WebhookHandler getHandler(String platform) {
        switch (platform) {
            case "track123":
                return new Track123WebhookHandler();
            default:
                throw new IllegalArgumentException("Unknown platform");
        }
    }
}
