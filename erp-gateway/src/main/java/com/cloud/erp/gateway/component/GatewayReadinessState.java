package com.cloud.erp.gateway.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gateway 本地 readiness 状态，不检查后端服务、Nacos、Redis 等外部依赖。
 */
@Component
public class GatewayReadinessState {

    private final AtomicBoolean applicationReady = new AtomicBoolean(false);
    private final AtomicBoolean preStopping = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady.set(true);
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        preStopping.set(true);
    }

    public boolean isReady() {
        return applicationReady.get() && !preStopping.get();
    }
}
