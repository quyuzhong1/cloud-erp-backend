package com.erp.server.dmp.factory;

import com.erp.server.dmp.handler.InboundHandler;
import com.erp.server.dmp.handler.OrderOutboundHandler;
import com.erp.server.dmp.handler.Track123WebhookHandler;
import com.erp.server.dmp.handler.WebhookHandler;
import jnr.ffi.annotations.In;
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
    public WebhookHandler getHandler(String service) {
        switch (service) {
            case "track123":
                return new Track123WebhookHandler();
            case "outbound":
                return new OrderOutboundHandler();
            case "inbound":
                return new InboundHandler();
            default:
                throw new IllegalArgumentException("Unknown platform");
        }
    }
}
