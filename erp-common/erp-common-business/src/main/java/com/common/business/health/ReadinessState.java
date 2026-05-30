package com.common.business.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 仅维护本进程 readiness 状态，外部依赖检查由探针按需触发。
 */
@Component
@ConditionalOnMissingBean(ReadinessState.class)
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class ReadinessState {

    private final AtomicBoolean applicationReady = new AtomicBoolean(false);
    private final AtomicBoolean preStopping = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady.set(true);
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        markPreStopping();
    }

    public boolean isApplicationReady() {
        return applicationReady.get();
    }

    public boolean isPreStopping() {
        return preStopping.get();
    }

    public void markPreStopping() {
        preStopping.set(true);
    }
}
